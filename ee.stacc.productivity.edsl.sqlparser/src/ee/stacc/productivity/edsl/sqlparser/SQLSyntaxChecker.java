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

import ee.stacc.productivity.edsl.lexer.automata.AutomataDeterminator;
import ee.stacc.productivity.edsl.lexer.automata.AutomataInclusion;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator;
import ee.stacc.productivity.edsl.lexer.automata.IAlphabetConverter;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator.IEmptinessExpert;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexer;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;

public class SQLSyntaxChecker {

	public static final SQLSyntaxChecker INSTANCE = new SQLSyntaxChecker();
	
	private SQLSyntaxChecker() {}
	
	public List<String> check(IAbstractString str) {
		if (hasLoops(str)) {
			throw new IllegalArgumentException("The current version does not support loops in abstract strings");
		}
		
		return checkAbstractString(str, SimpleStack.FACTORY);
		
//		return result 
//			? Collections.<String>emptyList()
//			: Collections.singletonList("SQL syntax error");
	}

	public List<String> checkAbstractString(IAbstractString as, IStackFactory stackFactory) {
		State asAut = StringToAutomatonConverter.INSTANCE.convert(as, SQLLexer.SQL_ALPHABET_CONVERTER);
		
		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		State transduction = AutomataInclusion.INSTANCE.getTrasduction(sqlTransducer, asAut);
		transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction, new IEmptinessExpert() {
			
			@Override
			public boolean isEmpty(Transition transition) {
				return transition.isEmpty() || SQLLexer.getTokenName(transition.getInChar()).length() == 0;
			}
		});
		transduction = AutomataDeterminator.determinate(transduction);
		
		final LRParser parser = Parsers.SQL_PARSER;
		
		final Integer eofTokenIndex = parser.getNamesToTokenNumbers().get("$end");
		IAlphabetConverter converter = new IAlphabetConverter() {
			
			@Override
			public int convert(int c) {
				if (c == (char) -1) {
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
		final List<String> errors = new ArrayList<String>();
		FixpointParser fixpointParser = new FixpointParser(parser, converter, SimpleStackSet.FACTORY, stackFactory, eofTokenIndex, new IParseErrorHandler() {
			
			@Override
			public void handleError(String message) {
				errors.add(message);
			}
		});
		boolean parseResult = fixpointParser.parse(transduction);
		if (!parseResult && errors.isEmpty()) {
			errors.add("SQL syntax error");
		}
		return errors;
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
	
	public interface IParseErrorHandler {
		IParseErrorHandler NONE = new IParseErrorHandler() {
			@Override
			public void handleError(String message) {
			}
		}; 
		
		IParseErrorHandler SYSTEM_OUT = new IParseErrorHandler() {
			@Override
			public void handleError(String message) {
				System.out.println(message);
			}
		}; 
		
		IParseErrorHandler EXCEPTION = new IParseErrorHandler() {
			@Override
			public void handleError(String message) {
				throw new IllegalArgumentException(message);
			}
		}; 
		
		void handleError(String message);
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
					IParserState errorState = setForCurrent.hasError();
					if (errorState != null) {
						int unexpectedSymbol = ((ErrorState) errorState).getUnexpectedSymbol();
						errorHandler.handleError("Unexpected token: " + Parsers.SQL_PARSER.getSymbolNumbersToNames().get(unexpectedSymbol));
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

	private static boolean hasLoops(IAbstractString str) {
		return str.accept(LOOP_FINDER, null);
	}

	private static final IAbstractStringVisitor<Boolean, Void> LOOP_FINDER = new IAbstractStringVisitor<Boolean, Void>() {

		@Override
		public Boolean visitStringCharacterSet(
				StringCharacterSet characterSet, Void data) {
			return false;
		}

		@Override
		public Boolean visitStringChoise(StringChoice stringChoise,
				Void data) {
			for (IAbstractString item : stringChoise.getItems()) {
				if (hasLoops(item)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Boolean visitStringConstant(StringConstant stringConstant,
				Void data) {
			return false;
		}

		@Override
		public Boolean visitStringRepetition(
				StringRepetition stringRepetition, Void data) {
			return true;
		}

		@Override
		public Boolean visitStringSequence(StringSequence stringSequence,
				Void data) {
			for (IAbstractString item : stringSequence.getItems()) {
				if (hasLoops(item)) {
					return true;
				}
			}
			return false;
		}
	};
}
