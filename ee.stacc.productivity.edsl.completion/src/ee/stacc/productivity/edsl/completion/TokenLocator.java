package ee.stacc.productivity.edsl.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.checkers.sqlstatic.PositionedCharacter;
import ee.stacc.productivity.edsl.checkers.sqlstatic.PositionedCharacterUtil;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence.IFoldFunction;
import ee.stacc.productivity.edsl.lexer.automata.AutomataTransduction;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator;
import ee.stacc.productivity.edsl.lexer.automata.IncomingTransitionsInitializer;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexer;
import ee.stacc.productivity.edsl.string.AbstractStringCollection;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringParameter;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;

/**
 * 
 * @author abreslav
 *
 */
public class TokenLocator {

	public static final TokenLocator INSTANCE = new TokenLocator();
	
	public static final class CompletionContext {
		private final State automaton;
		private final Collection<Transition> completeTokensToTheLeft = new ArrayList<Transition>();
		private final Map<Transition, String> incompleteIdsToTheLeft = new HashMap<Transition, String>();
		
		public CompletionContext(State automaton) {
			this.automaton = automaton;
		}
		
		public State getAutomaton() {
			return automaton;
		}
		
		public Collection<Transition> getCompleteTokensToTheLeft() {
			return completeTokensToTheLeft;
		}
		
		public Map<Transition, String> getIncompleteIdsToTheLeft() {
			return incompleteIdsToTheLeft;
		}
	}
	
	private TokenLocator() {}
	
	/**
	 * This method is used for debugging purposes only
	 */
	public Collection<IPosition> locateToken(String filePath, int offset) {
		CompletionContext completionContext = findCompletionContext(filePath, offset);
		if (completionContext == null) {
			return Collections.emptyList();
		}
		
		IncomingTransitionsInitializer.initializeIncomingTransitions(completionContext.getAutomaton());
		
		List<Transition> list = new ArrayList<Transition>(completionContext.getCompleteTokensToTheLeft());
		list.addAll(completionContext.getIncompleteIdsToTheLeft().keySet());
		
		for (Iterator<Transition> iterator = list.iterator(); iterator.hasNext();) {
			Transition transition = iterator.next();
			if (!ContextRecognizer.INSTANCE.isBetween(transition, SQLLexer.getCodeByName("SELECT"), SQLLexer.getCodeByName("FROM"))) {
				iterator.remove();
			}
		}
		
		return getPositions(list);
	}


	public CompletionContext findCompletionContext(String filePath, int offset) {
		IAbstractString abstractString = CacheService.getCacheService().getContainingAbstractString(filePath, offset);
		if (abstractString == null) {
			return null;
		}
		IAbstractString currentLiteral = findContainingLiteral(abstractString, filePath, offset);
		if (currentLiteral == null) {
			return null;
		}
		State automaton = PositionedCharacterUtil.createPositionedAutomaton(abstractString);
		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		State transduction = AutomataTransduction.INSTANCE.getTransduction(
				sqlTransducer, 
				automaton, 
				SQLLexer.SQL_ALPHABET_CONVERTER);
		transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(
				transduction);

		SearchResult closest = findCurrentTokens(transduction, currentLiteral, offset);

		CompletionContext result = new CompletionContext(transduction);
		for (Transition transition : closest.transitionsOn) {
			Token token = getToken(transition);
			int code = token.getCode();
			if (SQLLexer.isIdentifier(code)) {
				 // inside identifier
				result.getIncompleteIdsToTheLeft().put(transition, getTextPortion(getToken(transition), currentLiteral, offset));
			} else if (SQLLexer.isWhitespace(code)) {
				 // inside whitespace
				Collection<Transition> precedingTransitions = getPrecedingTransitions(transition);
				for (Transition prev : precedingTransitions) {
					Token prevToken = getToken(prev);
					if (SQLLexer.isIdentifier(prevToken.getCode())) {
						result.getIncompleteIdsToTheLeft().put(prev, PositionedCharacterUtil.render(prevToken));						
					} else {
						result.getCompleteTokensToTheLeft().add(prev);
					}
				}
			} else {
				// inside some other token
				// TODO: Handle keywords here?
			}
		}
		if (closest.transitionsOn.isEmpty()) {
			for (Transition transition : closest.transitionsBefore) {
				Token token = getToken(transition);
				int code = token.getCode();
				if (SQLLexer.isIdentifier(code)) {
					 // after identifier
					result.getIncompleteIdsToTheLeft().put(transition, PositionedCharacterUtil.render(token));
				} else if (SQLLexer.isWhitespace(code)) {
					 // after whitespace
					result.getCompleteTokensToTheLeft().addAll(getPrecedingTransitions(transition));
				} else {
					// after some other token
					// TODO: Handle keywords here?
					result.getCompleteTokensToTheLeft().add(transition);
				}
			}
		}
		return result;
	}

	private String getTextPortion(Token token, final IAbstractString currentLiteral, final int offset) {
		// If we have been inside the currentLiteral and left it, we should not add more characters
		// This is needed for the case, where the cursor stands at the end of the literal: "abc|" + "cde"
		final boolean[] beenInCurrentLiteral = new boolean[] {false};
		// When we reach the current offset or leave the current literal, 
		// this flag is set, and we do not add more characters
		final boolean[] stop = new boolean[] {false};
		final IPosition currentLiteralPosition = currentLiteral.getPosition();
		return token.getText().fold(new StringBuilder(), new IFoldFunction<StringBuilder, IAbstractInputItem>() {

			@Override
			public StringBuilder body(StringBuilder init,
					IAbstractInputItem arg, boolean last) {
				if (stop[0]) {
					return init;
				}
				PositionedCharacter c = (PositionedCharacter) arg;
				IPosition stringPosition = c.getStringPosition();
				if (stringPosition.equals(currentLiteralPosition)) {
					// TODO: This does not take into account characters with 
					// source length > 1 (e.g. \u0020), but these are quite unlikely 
					// to appear inside an identifier :)
					int cStart = stringPosition.getStart() + c.getIndexInString();
					if (cStart == offset) {
						stop[0] = true;
						return init;
					}
					beenInCurrentLiteral[0] = true;
				} else { 
					if (beenInCurrentLiteral[0]) {
						stop[0] = true;
						return init;
					}
				}
				init.append((char) c.getCode());
				return init;
			}
		}).toString();
	}


	private Collection<IPosition> getPositions(
			Collection<Transition> transitions) {
		Collection<IPosition> positions = new ArrayList<IPosition>(); 
		for (Transition transition : transitions) {
			Token token = getToken(transition);
			positions.addAll(PositionedCharacterUtil.getMarkerPositions(token.getText()));
		}
		return positions;
	}


	public static Token getToken(Transition transition) {
		Token token = (Token) transition.getInChar();
		return token;
	}


	private Collection<Transition> getPrecedingTransitions(Transition transition) {
		Collection<Transition> result = new ArrayList<Transition>();
		Iterable<Transition> incomingTransitions = transition.getFrom().getIncomingTransitions();
		for (Transition prev : incomingTransitions) {
			result.add(prev);
		}
		return result;
	}


	private static final class SearchResult {
		// Transitions containing tokens, inside which the cursor stands 
		// (chars from the token are to the left and to the right of the cursor)
		private final Collection<Transition> transitionsOn = new ArrayList<Transition>();
		// Tokens immediately to the left of the cursor:
		// the last char of the token has offset = cursorOffs - 1
		private final Collection<Transition> transitionsBefore = new ArrayList<Transition>();
		// Tokens immediately to the right of the cursor:
		// the first char of the token has offset = cursorOffs
		private final Collection<Transition> transitionsAfter = new ArrayList<Transition>();
	}
	
	private SearchResult findCurrentTokens(State state, final IAbstractString currentLiteral, final int cursorOffs) {
		final SearchResult closest = new SearchResult();
		
		Queue<State> queue = new LinkedList<State>();
		queue.offer(state);
		Set<State> visited = new HashSet<State>();
		visited.add(state);
		while (!queue.isEmpty()) {
			State s = queue.poll();
			for (final Transition transition : s.getOutgoingTransitions()) {
				final Token token = getToken(transition);
				ChunkPosition chunkPosition = ChunkPosition.getChunkPositionFromLiteral(currentLiteral, token);
				if (chunkPosition != null) {
					if (chunkPosition.tokenInside(cursorOffs)) {
						closest.transitionsOn.add(transition);
					} 
					if (chunkPosition.tokenLeftSide(cursorOffs)) {
						closest.transitionsAfter.add(transition);
					}
					if (chunkPosition.tokenRightSide(cursorOffs)) {
						closest.transitionsBefore.add(transition);
					}
				}
				State to = transition.getTo();
				if (!visited.contains(to)) {
					queue.offer(to);
					visited.add(to);
				}
			}
			
		}
		return closest;
	}
	
	private IAbstractString findContainingLiteral(
			IAbstractString abstractString, final String filePath, final int offset) {
		return abstractString.accept(new IAbstractStringVisitor<IAbstractString, Void>() {

			@Override
			public IAbstractString visitStringCharacterSet(
					StringCharacterSet characterSet, Void data) {
				return null;
			}

			@Override
			public IAbstractString visitStringChoice(StringChoice stringChoice,
					Void data) {
				return visitCollection(stringChoice);
			}

			private IAbstractString visitCollection(
					AbstractStringCollection collection) {
				for (IAbstractString s : collection.getItems()) {
					IAbstractString r = s.accept(this, null);
					if (r != null) {
						return r;
					}
				}
				return null;
			}

			@Override
			public IAbstractString visitStringConstant(
					StringConstant stringConstant, Void data) {
				IPosition position = stringConstant.getPosition();
				if (position.getStart() < offset 
						&& offset < position.getStart() + position.getLength() 
						&& filePath.equals(position.getPath())) {
					return stringConstant;
				}
				return null;
			}

			@Override
			public IAbstractString visitStringParameter(
					StringParameter stringParameter, Void data) {
				throw new IllegalStateException();
			}

			@Override
			public IAbstractString visitStringRepetition(
					StringRepetition stringRepetition, Void data) {
				return stringRepetition.accept(this, null);
			}

			@Override
			public IAbstractString visitStringSequence(
					StringSequence stringSequence, Void data) {
				return visitCollection(stringSequence);
			}
		}, null);
	}

	private static final class ChunkPosition {
		private int start;
		private int length;
		private boolean leftInTheMiddle; // left side of the chunk is in the middle of the token
		private boolean rightInTheMiddle; // right side of the chunk is in the middle of the token 
		
		private int processed;
		private Integer firstIndex;
		
		public boolean tokenInside(int offset) {
			return start < offset && offset < start + length
				|| leftInTheMiddle && leftSide(offset)
				|| rightInTheMiddle && rightSide(offset);
		}

		public boolean tokenLeftSide(int offset) {
			return !leftInTheMiddle && leftSide(offset);
		}
		
		public boolean tokenRightSide(int offset) {
			return !rightInTheMiddle && rightSide(offset);
		}
		
		private boolean rightSide(int offset) {
			return offset == start + length;
		}
		
		private boolean leftSide(int offset) {
			return offset == start;
		}

		private static ChunkPosition getChunkPositionFromLiteral(
				final IAbstractString currentLiteral, final Token token) {
			ChunkPosition chunkPosition = token.getText().fold(new ChunkPosition(), new IFoldFunction<ChunkPosition, IAbstractInputItem>() {
				
				@Override
				public ChunkPosition body(ChunkPosition position, IAbstractInputItem arg, boolean last) {
					PositionedCharacter c = (PositionedCharacter) arg;
					IPosition stringPos = c.getStringPosition();
					if (stringPos.equals(currentLiteral.getPosition())) {
						int strStart = stringPos.getStart();
						int offs = strStart + c.getIndexInString();
						int len = c.getLengthInSource();
						if (position.firstIndex == null) {
							position.firstIndex = position.processed;
							position.start = offs;
							position.length = len;
							position.leftInTheMiddle = position.firstIndex > 0;
						} else {
							position.length += len;
						}
					} else {
						if (last && position.firstIndex != null) {
							// Last index is less than the size, because we are in this branch
							position.rightInTheMiddle = true;
						}
					}
					position.processed++;
					return position;
				}
			});
			return chunkPosition;
		}
	}

}
