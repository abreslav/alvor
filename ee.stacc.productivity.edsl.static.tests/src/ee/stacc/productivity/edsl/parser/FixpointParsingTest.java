package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.automata.AutomataConverter;
import ee.stacc.productivity.edsl.lexer.automata.AutomataDeterminator;
import ee.stacc.productivity.edsl.lexer.automata.AutomataInclusion;
import ee.stacc.productivity.edsl.lexer.automata.AutomataParser;
import ee.stacc.productivity.edsl.lexer.automata.AutomataUtils;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator;
import ee.stacc.productivity.edsl.lexer.automata.IAlphabetConverter;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator.IEmptinessExpert;
import ee.stacc.productivity.edsl.sqllexer.SQLLexerData;
import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;


public class FixpointParsingTest {
	private static final IAbstractStackSetFactory FACTORY = new IAbstractStackSetFactory() {
		
		@Override
		public IAbstractStackSet newAbstractStackSet() {
			return new StackSet();
		}
	};

	private static IStackFactory STACK_FACTORY = SimpleFoldedStack.FACTORY;
	
	private static LRParser parser = Parsers.SQL_PARSER;
	
	@Test
	public void testSimple() throws Exception {
//		fail();
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
		}, FACTORY, parser.getNamesToTokenNumbers().get("$end")).parse(initial);
		return parse;
	}
	
	@Test
	public void testSQL() throws Exception {
//		fail();
		String abstractString;

		abstractString = "\"SELECT sd, asd FROM asd\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd FROM asd, dsd\"";
		assertTrue(parseAbstractString(abstractString));

		
		abstractString = "\"SELECT asd, dsd FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd FROM asd\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd asd FROM asd\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"asd FROM asd\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd FROM asd,\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd FROM ,\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd\" (\", dsd\")+ \"FROM asd, sdf\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd\" {\", dsd\", \"\"} \" FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd\" {\", dsd\", \"\", \", a, s, v\"} \" FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
		
	}

	@Test
	public void testLoops() throws Exception {
		String abstractString;

		abstractString = "\"SELECT asd\" (\", dsd \")+ \"FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd\" (\", dsd \")+ \"sd FROM asd, sdf\"";
		assertFalse(parseAbstractString(abstractString));
		
	}
	
	private boolean parseAbstractString(String abstractString) {
		State sqlTransducer = AutomataConverter.INSTANCE.convert();
//		AutomataUtils.printSQLAutomaton(sqlTransducer);
		
		IAbstractString as = AbstractStringParser.parseOneFromString(abstractString);
		State asAut = StringToAutomatonConverter.INSTANCE.convert(as, AutomataUtils.SQL_ALPHABET_CONVERTER);
		asAut = AutomataDeterminator.determinate(asAut);
//		AutomataUtils.printAutomaton(asAut, AutomataUtils.SQL_IN_MAPPER, AutomataUtils.ID_MAPPER);
		
		State transduction = AutomataInclusion.INSTANCE.getTrasduction(sqlTransducer, asAut);
		transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction, new IEmptinessExpert() {
			
			@Override
			public boolean isEmpty(Transition transition) {
				return transition.isEmpty() || AutomataUtils.SQL_TOKEN_MAPPER.map(transition.getInChar()).length() == 0;
			}
		});
		transduction = AutomataDeterminator.determinate(transduction);
//		AutomataUtils.printAutomaton(transduction, AutomataUtils.SQL_TOKEN_MAPPER, AutomataUtils.ID_MAPPER);
		
		final Integer eofTokenIndex = parser.getNamesToTokenNumbers().get("$end");
		IAlphabetConverter converter = new IAlphabetConverter() {
			
			@Override
			public int convert(int c) {
				if (c == (char) -1) {
					return eofTokenIndex;
				}
				String tokenName = SQLLexerData.TOKENS[c];
//				System.out.println(c);
				if (Character.isLetter(tokenName.charAt(0))) {
					return parser.getNamesToTokenNumbers().get(tokenName);
				}
				return parser.getNamesToTokenNumbers().get("'" + tokenName + "'");
			}
		};
		FixpointParser fixpointParser = new FixpointParser(parser, converter , FACTORY, eofTokenIndex);
		boolean parseResult = fixpointParser.parse(transduction);
		return parseResult;
	}
	
	public interface IAbstractStackSet {
		
		/**
		 * @return true if there was an actual change 
		 */
		boolean add(IAbstractStack stack);
		
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
//			System.out.println("Set #" + System.identityHashCode(this));
//			System.out.println(stack);
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

		public boolean parse(State initial) {
			IAbstractStack initialStack = STACK_FACTORY.newStack(parser.getInitialState());
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
				if (transition.isEmpty()) {
					throw new IllegalArgumentException("Empty transitions are not supported");
				}
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
