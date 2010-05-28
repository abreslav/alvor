package ee.stacc.productivity.edsl.sqlparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

public class ParserSimulator<S extends IParserStackLike> {

//	public static final ParserSimulator<IParserStack> LALR_INSTANCE = new ParserSimulator<IParserStack>(Parsers.SQL_PARSER, BoundedStack.getFactory(100, null));
	public static final ParserSimulator<GLRStack> LALR_INSTANCE = new ParserSimulator<GLRStack>(Parsers.SQL_GLR_PARSER, GLRStack.FACTORY);
	
	private final IStackFactory<S> stackFactory;
	private final ILRParser<S> parser;
	public long allTime;

	public ParserSimulator(ILRParser<S> parser, IStackFactory<S> factory) {
		this.stackFactory = factory;
		this.parser = parser;
	}

	public List<String> check(IAbstractString str) {
//		if (AsbtractStringUtils.hasLoops(str)) {
//			throw new IllegalArgumentException("The current version does not support loops in abstract strings");
//		}
		
		return checkAbstractString(str);
	}
	
	public List<String> checkAbstractString(IAbstractString as) {
		final List<String> errors = new ArrayList<String>();
		State asAut = StringToAutomatonConverter.INSTANCE.convert(as);
		
		checkAutomaton(parser, asAut, stackFactory, new IParseErrorHandler() {
			
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
			
			@Override
			public void overabstraction() {
				errors.add("Syntax analysis failed: recursion is too deep");
			}
		});
		return errors;
	}

	public void checkAutomaton(State asAut, IParseErrorHandler errorHandler) {
		checkAutomaton(parser, asAut, stackFactory, errorHandler);
	}
	
	private void checkAutomaton(final ILRParser<S> parser, State asAut, IStackFactory<S> stackFactory, IParseErrorHandler errorHandler) {
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
					tokenNumber = parser.getNamesToTokenNumbers().get("error");
					if (tokenNumber == null) {
						throw new IllegalArgumentException("Unknown token: " + tokenName);
					}
				}
				return tokenNumber;
			}
		};
		FixpointParser<S> fixpointParser = new FixpointParser<S>(parser, converter, SimpleStackSet.<S>getFactory(), stackFactory, eofTokenIndex, errorHandler);
		fixpointParser.parse(transduction);
		time = System.nanoTime() - time;
		allTime += time;
	}
	
	public boolean parseAutomaton(State initial, IAlphabetConverter alphabetConverter) {//, IStackFactory<S> stackFactory) {
		return new FixpointParser<S>(
				parser, 
				alphabetConverter, 
				SimpleStackSet.<S>getFactory(), 
				stackFactory, 
				parser.getEofTokenIndex(),
				IParseErrorHandler.NONE)
			.parse(initial);
	}
	
	private interface IStackSet<S> {

		IParserState hasError();
		
		/**
		 * @return true if there was an actual change 
		 */
		boolean add(S stack);
		
		/**
		 * @return a java.util.Set of all IAbstractStack objects, which is 
		 *         tolerant for concurrent modification (e.g., a copy)
		 */
		Set<S> asJavaSet();
	}

	private static class SimpleStackSet<S extends IParserStackLike> implements IStackSet<S> {
		
		public static <S extends IParserStackLike> IStackSetFactory<S> getFactory() {
			return new IStackSetFactory<S>() {
				
				@Override
				public IStackSet<S> newAbstractStackSet() {
					return new SimpleStackSet<S>();
				}
			};
		}

		private final Set<S> stacks = new HashSet<S>();
		
		@Override
		public IParserState hasError() {
			for (S stack : stacks) {
				IParserState errorOnTop = stack.getErrorOnTop();
				if (errorOnTop != null) {
					return errorOnTop;
				}
			}
			return null;
		}

		@Override
		public Set<S> asJavaSet() {
			return new HashSet<S>(stacks);
		}

		@Override
		public boolean add(S stack) {
			return stacks.add(stack);
		}
		
		@Override
		public String toString() {
			return stacks.toString();
		}
		
	}
	
	public interface IStackSetFactory<S> {
		IStackSet<S> newAbstractStackSet();
	}
	
	private static final class FixpointParser<S extends IParserStackLike> {
	
		private final Map<State, IStackSet<S>> abstractStackSets = new HashMap<State, IStackSet<S>>();
		private final IAlphabetConverter alphabetConverter;
		private final ILRParser<S> parser;
		private final IStackSetFactory<S> factory;
		private final IStackFactory<S> stackFactory;
		private final int eofTokenIndex;
		private final IParseErrorHandler errorHandler; 
		
		public FixpointParser(ILRParser<S> parser,
				IAlphabetConverter alphabetConverter,
				IStackSetFactory<S> factory,
				IStackFactory<S> stackFactory,
				int eofTokenIndex,
				IParseErrorHandler parseErrorHandler) {
			this.parser = parser;
			this.alphabetConverter = alphabetConverter;
			this.factory = factory;
			this.stackFactory = stackFactory;
			this.eofTokenIndex = eofTokenIndex;
			this.errorHandler = parseErrorHandler;
		}
		
		private IStackSet<S> getSet(State state) {
			IStackSet<S> set = abstractStackSets.get(state);
			if (set == null) {
				set = factory.newAbstractStackSet();
				abstractStackSets.put(state, set);
			}
			return set;
		}

		public boolean parse(State initial) {
			S initialStack = stackFactory.newStack(parser.getInitialState());
			getSet(initial).add(initialStack);
			return dfs(initial);
		}
	
		private boolean dfs(State current) {
			IStackSet<S> setForCurrent = getSet(current);
			
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
				IStackSet<S> setForTo = getSet(transition.getTo());
				boolean changes = transformSet(setForCurrent, transition.getInChar(), tokenIndex, setForTo);
				if (changes) {
					IParserState errorState = setForTo.hasError();
					if (errorState != null) {
						IAbstractInputItem unexpectedItem = ((ErrorState) errorState).getUnexpectedItem();
						if (unexpectedItem == null) {
							errorHandler.overabstraction();
						} else if (unexpectedItem.getCode() >= 0) {
							errorHandler.unexpectedItem(unexpectedItem);
						} else {
							errorHandler.other();
						}
						return false;
					}
					if (!dfs(transition.getTo())) {
						return false;
					}
				}
			}
			
			return true;
		}

		private boolean transformSet(IStackSet<S> setForCurrent,
				IAbstractInputItem token, int tokenIndex, IStackSet<S> setForTo) {
			Collection<S> hashSet = setForCurrent.asJavaSet();
 			boolean changes = false;
			for (S stack : hashSet) {
				S newStack = parser.processToken(token, tokenIndex, stack);
				if (setForTo.add(newStack)) {
					changes = true;
				}
			}
			return changes;
		}

		private boolean closeWithEof(IStackSet<S> setForCurrent) {
			for (S stack : setForCurrent.asJavaSet()) {
				while (true) {
					S newStack = parser.processToken(IAbstractInputItem.EOF, eofTokenIndex, stack);
					if (newStack.hasErrorOnTop()) {
						return false;
					}
					if (newStack.topAccepts()) {
						break;
					}
					stack = newStack;
				}
			}
			return true;
		}
	}
}
