package ee.stacc.productivity.edsl.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.checkers.sqlstatic.PositionedCharacter;
import ee.stacc.productivity.edsl.checkers.sqlstatic.PositionedCharacterUtil;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence.IFoldFunction;
import ee.stacc.productivity.edsl.lexer.automata.AutomataInclusion;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator.IEmptinessExpert;
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

public class TokenLocator {

	public static final TokenLocator INSTANCE = new TokenLocator();
	
	public Collection<IPosition> locateToken(String filePath, int offset) {
		IAbstractString abstractString = CacheService.getCacheService().getContainingAbstractString(filePath, offset);
		if (abstractString == null) {
			return Collections.emptyList();
		}
		IAbstractString currentLiteral = findContainingLiteral(abstractString, filePath, offset);
		if (currentLiteral == null) {
			return Collections.emptyList();
		}
		State automaton = PositionedCharacterUtil.createPositionedAutomaton(abstractString);
		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		State transduction = AutomataInclusion.INSTANCE.getTrasduction(
				sqlTransducer, 
				automaton, 
				SQLLexer.SQL_ALPHABET_CONVERTER);
		transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(
				transduction, 
				new IEmptinessExpert() {
			
			@Override
			public boolean isEmpty(Transition transition) {
				return transition.isEmpty();
			}
		});

		SearchState closest = findCurrentTokens(transduction, currentLiteral, offset);
//		if (closest.transition == null) {
//			return Collections.emptyList();
//		}
//		
//		if (closest.transitionsOn) {
//			Token token = (Token) closest.transition.getInChar();
//			return PositionedCharacterUtil.getMarkerPositions(token.getText());
//		}
		
		Collection<IPosition> positions = new ArrayList<IPosition>(); 
		for (Transition transition : closest.transitionsAfter) {
			Token token = (Token) transition.getInChar();
//			System.out.println(token);
			positions.addAll(PositionedCharacterUtil.getMarkerPositions(token.getText()));
		}
		return positions;
	}


	private Collection<Token> getPrecedingTokens(Collection<Transition> transitions) {
		Collection<Token> result = new ArrayList<Token>();
		for (Transition transition : transitions) {
			Iterable<Transition> incomingTransitions = transition.getFrom().getIncomingTransitions();
			for (Transition prev : incomingTransitions) {
				Token prevToken = (Token) prev.getInChar();
				result.add(prevToken);
			}
		}
		return result;
	}


	private final class SearchState {
		private Collection<Transition> transitionsOn = new ArrayList<Transition>();
		private Collection<Transition> transitionsBefore = new ArrayList<Transition>();
		private Collection<Transition> transitionsAfter = new ArrayList<Transition>();
	}
	
	private SearchState findCurrentTokens(State state, final IAbstractString currentLiteral, final int offset) {

		final SearchState closest = new SearchState();
		
		Queue<State> queue = new LinkedList<State>();
		queue.offer(state);
		Set<State> visited = new HashSet<State>();
		visited.add(state);
		while (!queue.isEmpty()) {
			State s = queue.poll();
			for (final Transition transition : s.getOutgoingTransitions()) {
				final Token token = (Token) transition.getInChar();
				ChunkPosition chunkPosition = getChunkPositionFromLiteral(currentLiteral, token);
				if (chunkPosition != null) {
					if (chunkPosition.tokenInside(offset)) {
						closest.transitionsOn.add(transition);
					} 
					if (chunkPosition.tokenLeftSide(offset)) {
						closest.transitionsAfter.add(transition);
					}
					if (chunkPosition.tokenRightSide(offset)) {
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

	private final class ChunkPosition {
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
	}

	private ChunkPosition getChunkPositionFromLiteral(
			final IAbstractString currentLiteral, final Token token) {
		ChunkPosition tokenPosition = token.getText().fold(new ChunkPosition(), new IFoldFunction<ChunkPosition, IAbstractInputItem>() {

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
		return tokenPosition;
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
}
