package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.automata.AutomataParser;
import ee.stacc.productivity.edsl.lexer.automata.AutomataUtils;
import ee.stacc.productivity.edsl.lexer.automata.IAlphabetConverter;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;


public class FixpointParsingTest {

	@Test
	public void test() throws Exception {
		final LRParser parser = LRParser.build("../ee.stacc.productivity.edsl.sqlparser/generated/sql.xml");
		State initial;
		
		initial = AutomataParser.parse("A - B:S; B - C:I; C - D:F; D - E:I; E - !X:X");
		AutomataUtils.generate(initial, "");
		assertTrue(doParse(parser, initial));
		
		initial = AutomataParser.parse("A - B:S; B - C:S; C - D:F; D - E:I; E - !X:X");
		assertFalse(doParse(parser, initial));
		
	}

	private boolean doParse(final LRParser parser, State initial) {
		boolean parse = new FixpointParser(parser, new IAlphabetConverter() {
			
			@Override
			public int convert(int c) {
				Map<String, Integer> namesToTokenNumbers = parser.getNamesToTokenNumbers();
				switch (c) {
				case 'S':
					return namesToTokenNumbers.get("SELECT");
				case 'I':
					return namesToTokenNumbers.get("ID");
				case 'F':
					return namesToTokenNumbers.get("FROM");
				case 'X':
					return namesToTokenNumbers.get("$end");
				case ',':
					return namesToTokenNumbers.get("','");
				}
				throw new IllegalStateException("Unknown token: " + c);
			}
		}, new IAbstractStackSetFactory() {
			
			@Override
			public IAbstractStackSet newAbstractStackSet() {
				return new StackSet();
			}
		}, parser.getNamesToTokenNumbers().get("$end")).parse(initial);
		return parse;
	}
	
	public interface IAbstractStackSet {
		
		/**
		 * @return true if there was an actual change 
		 */
		boolean add(IAbstractStack stack);
		
		boolean allAccepting();
		boolean hasError();
		/**
		 * @return a java.util.Set of all IAbstractStack objects, which is 
		 *         tolerant for concurrent modification (e.g., a copy)
		 */
		Set<IAbstractStack> asJavaSet();
	}

	private static class StackSet implements IAbstractStackSet {

		private final Set<IAbstractStack> stacks = new HashSet<IAbstractStack>();
		
		@Override
		public boolean allAccepting() {
			for (IAbstractStack stack : stacks) {
				if (stack.top() != IParserState.ACCEPT) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean hasError() {
			for (IAbstractStack stack : stacks) {
				if (stack.top() == IParserState.ERROR) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Set<IAbstractStack> asJavaSet() {
			return new HashSet<IAbstractStack>(stacks);
		}

		@Override
		public boolean add(IAbstractStack stack) {
			return stacks.add(stack);
		}
		
	}
	
	public interface IAbstractStackSetFactory {
		IAbstractStackSet newAbstractStackSet();
	}
	
	private static final class FixpointParser {
	
		private final Map<State, IAbstractStackSet> abstractStackSets = new HashMap<State, IAbstractStackSet>();
		private final IAlphabetConverter alphabetConverter;
		private final LRParser parser;
		private final IAbstractStackSetFactory factory;
		private final int eofTokenIndex;
		
		public FixpointParser(LRParser parser,
				IAlphabetConverter alphabetConverter,
				IAbstractStackSetFactory factory,
				int eofTokenIndex) {
			this.parser = parser;
			this.alphabetConverter = alphabetConverter;
			this.factory = factory;
			this.eofTokenIndex = eofTokenIndex;
		}
		
		private IAbstractStackSet getSet(State state) {
			IAbstractStackSet set = abstractStackSets.get(state);
			if (set == null) {
				set = factory.newAbstractStackSet();
				abstractStackSets.put(state, set);
			}
			return set;
		}

		public boolean parse (State initial) {
			IAbstractStack initialStack = new SimpleStack(parser.getInitialState());
			getSet(initial).add(initialStack);
			return dfs(initial);
		}
	
		private boolean dfs(State current) {
			IAbstractStackSet setForCurrent = getSet(current);
			
			if (current.isAccepting()) {
				if (!closeWithEof(setForCurrent)) {
					return false;
				}
			}
			
			for (Transition transition : current.getOutgoingTransitions()) {
				int tokenIndex = alphabetConverter.convert(transition.getInChar());
				IAbstractStackSet setForTo = getSet(transition.getTo());
				boolean changes = transformSet(setForCurrent, tokenIndex, setForTo);
				if (changes) {
					if (setForCurrent.hasError()) {
						return false;
					}
					if (!dfs(transition.getTo())) {
						return false;
					}
				}
			}
			
			return true;
		}

		private boolean transformSet(IAbstractStackSet setForCurrent,
				int tokenIndex, IAbstractStackSet setForTo) {
			Collection<IAbstractStack> hashSet = setForCurrent.asJavaSet();
 			boolean changes = false;
			for (IAbstractStack stack : hashSet) {
				Set<IAbstractStack> newStacks = parser.processToken(tokenIndex, stack);
				for (IAbstractStack newStack : newStacks) {
					if (setForTo.add(newStack)) {
						changes = true;
					}
				}
			}
			return changes;
		}

		private boolean closeWithEof(IAbstractStackSet setForCurrent) {
			Queue<IAbstractStack> queue = new LinkedList<IAbstractStack>(setForCurrent.asJavaSet());
			while (!queue.isEmpty()) {
				IAbstractStack stack = queue.poll();
				
				Set<IAbstractStack> newStacks = parser.processToken(eofTokenIndex, stack);
				for (IAbstractStack newStack : newStacks) {
					IParserState top = newStack.top();
					if (top == IParserState.ERROR) {
						return false;
					}
					if (top != IParserState.ACCEPT) {
						queue.offer(newStack);
					}
				}
			}
			return true;
		}
	}
}
