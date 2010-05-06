package ee.stacc.productivity.edsl.sqlparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.automata.AutomataInclusion;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator;
import ee.stacc.productivity.edsl.lexer.automata.IAlphabetConverter;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator.IEmptinessExpert;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexer;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.util.AsbtractStringUtils;

public class SQLSyntaxChecker {

	private static final IStackFactory FACTORY = BoundedStack.getFactory(100, null);
//	private static final IStackFactory FACTORY = SimpleLinkedStack.FACTORY;
	public static final SQLSyntaxChecker INSTANCE = new SQLSyntaxChecker();
	
	private SQLSyntaxChecker() {}
	
	public long allTime;
	
	public List<String> check(IAbstractString str) {
		if (AsbtractStringUtils.hasLoops(str)) {
			throw new IllegalArgumentException("The current version does not support loops in abstract strings");
		}
		
		return checkAbstractString(str, FACTORY);
	}
	
	public List<String> checkAbstractString(IAbstractString as, IStackFactory stackFactory) {
		final List<String> errors = new ArrayList<String>();
		State asAut = StringToAutomatonConverter.INSTANCE.convert(as);
		
		checkAutomaton(asAut, stackFactory, new IParseErrorHandler() {
			
			@Override
			public void unexpectedItem(IAbstractInputItem item) {
				if (item instanceof Token) {
					Token token = (Token) item;
					errors.add("Unexpected token: " + SQLLexer.tokenToString(token));
				} else {
					errors.add("Unexpected token: " + Parsers.SQL_PARSER.getSymbolNumbersToNames().get(item.getCode()));
				}
			}
			
			@Override
			public void other() {
				if (errors.isEmpty()) {
					errors.add("SQL syntax error. Most likely unfinished query");
				}
			}
		});
		return errors;
	}

	public void checkAutomaton(State asAut, IParseErrorHandler errorHandler) {
		checkAutomaton(asAut, FACTORY, errorHandler);
	}
	
	private void checkAutomaton(State asAut, IStackFactory stackFactory, IParseErrorHandler errorHandler) {
		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		State transduction = AutomataInclusion.INSTANCE.getTrasduction(sqlTransducer, asAut, SQLLexer.SQL_ALPHABET_CONVERTER);
		long time = System.nanoTime();
		transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction, new IEmptinessExpert() {
			
			@Override
			public boolean isEmpty(Transition transition) {
				return transition.isEmpty() || SQLLexer.isWhitespace(transition.getInChar().getCode());
			}
		});
//		transduction = AutomataDeterminator.determinate(transduction);
		
		final LRParser parser = Parsers.SQL_PARSER;
		
		final Integer eofTokenIndex = parser.getNamesToTokenNumbers().get("$end");
		IAlphabetConverter converter = new IAlphabetConverter() {
			
			@Override
			public int convert(int c) {
				if (c == -1) {
					return eofTokenIndex;
				}
				String tokenName = SQLLexer.getTokenName(c);
				if (Character.isLetter(tokenName.charAt(0))) {
					return parser.getNamesToTokenNumbers().get(tokenName);
				}
				Integer tokenNumber = parser.getNamesToTokenNumbers().get("'" + tokenName + "'");
				if (tokenNumber == null) {
					throw new IllegalArgumentException("Unknown token: " + tokenName);
				}
				return tokenNumber;
			}
		};
		FixpointParser fixpointParser = new FixpointParser(parser, converter, SimpleStackSet.FACTORY, stackFactory, eofTokenIndex, errorHandler);
		fixpointParser.parse(transduction);
		time = System.nanoTime() - time;
		allTime += time;
	}
	
	public boolean parseAutomaton(State initial, IAlphabetConverter alphabetConverter, IStackFactory stackFactory) {
		return new FixpointParser(
				Parsers.SQL_PARSER, 
				alphabetConverter, 
				SimpleStackSet.FACTORY, 
				stackFactory, 
				Parsers.SQL_PARSER.getNamesToTokenNumbers().get("$end"),
				IParseErrorHandler.NONE)
			.parse(initial);
	}
	
	private interface IAbstractStackSet {

		IParserState hasError();
		
		/**
		 * @return true if there was an actual change 
		 */
		boolean add(IAbstractStack stack);
		
		/**
		 * @return a java.util.Set of all IAbstractStack objects, which is 
		 *         tolerant for concurrent modification (e.g., a copy)
		 */
		Set<IAbstractStack> asJavaSet();
	}

	private static class SimpleStackSet implements IAbstractStackSet {

		private static final IAbstractStackSetFactory FACTORY = new IAbstractStackSetFactory() {
			
			@Override
			public IAbstractStackSet newAbstractStackSet() {
				return new SimpleStackSet();
			}
		};

		private final Set<IAbstractStack> stacks = new HashSet<IAbstractStack>();
		
		@Override
		public IParserState hasError() {
			for (IAbstractStack stack : stacks) {
				IParserState top = stack.top();
				if (top.isError()) {
					return top;
				}
			}
			return null;
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
		private final IStackFactory stackFactory;
		private final int eofTokenIndex;
		private final IParseErrorHandler errorHandler; 
		
		public FixpointParser(LRParser parser,
				IAlphabetConverter alphabetConverter,
				IAbstractStackSetFactory factory,
				IStackFactory stackFactory,
				int eofTokenIndex,
				IParseErrorHandler parseErrorHandler) {
			this.parser = parser;
			this.alphabetConverter = alphabetConverter;
			this.factory = factory;
			this.stackFactory = stackFactory;
			this.eofTokenIndex = eofTokenIndex;
			this.errorHandler = parseErrorHandler;
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
			IAbstractStack initialStack = stackFactory.newStack(parser.getInitialState());
			getSet(initial).add(initialStack);
			return dfs(initial);
		}
	
		private boolean dfs(State current) {
			IAbstractStackSet setForCurrent = getSet(current);
			
			if (current.isAccepting()) {
				if (!closeWithEof(setForCurrent)) {
					errorHandler.other();
					return false;
				}
			}
			
			for (Transition transition : current.getOutgoingTransitions()) {
				if (transition.isEmpty()) {
					throw new IllegalArgumentException("Empty transitions are not supported");
				}
				int tokenIndex = alphabetConverter.convert(transition.getInChar().getCode());
				IAbstractStackSet setForTo = getSet(transition.getTo());
				boolean changes = transformSet(setForCurrent, transition.getInChar(), tokenIndex, setForTo);
				if (changes) {
					IParserState errorState = setForCurrent.hasError();
					if (errorState != null) {
						IAbstractInputItem unexpectedItem = ((ErrorState) errorState).getUnexpectedItem();
						errorHandler.unexpectedItem(unexpectedItem);
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
				IAbstractInputItem token, int tokenIndex, IAbstractStackSet setForTo) {
			Collection<IAbstractStack> hashSet = setForCurrent.asJavaSet();
 			boolean changes = false;
			for (IAbstractStack stack : hashSet) {
				Set<IAbstractStack> newStacks = parser.processToken(token, tokenIndex, stack);
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
				
				Set<IAbstractStack> newStacks = parser.processToken(IAbstractInputItem.EOF, eofTokenIndex, stack);
				for (IAbstractStack newStack : newStacks) {
					IParserState top = newStack.top();
					if (top.isError()) {
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
