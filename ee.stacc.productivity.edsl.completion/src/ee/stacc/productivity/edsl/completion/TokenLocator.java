package ee.stacc.productivity.edsl.completion;

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
import ee.stacc.productivity.edsl.string.Position;
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
				// || SQLLexer.getTokenName(transition.getInChar().getCode()).length() == 0;
			}
		});

		SearchState closest = findCurrentToken(transduction, currentLiteral, offset);
		if (closest.token == null) {
			return null;
		}
		
		if (closest.inside) {
			return PositionedCharacterUtil.getMarkerPositions(closest.token.getText());
		}
		
		Collection<Token> precedingTokens = getPrecedingTokens(closest.transition);
		
		return Collections.<IPosition>singleton(currentLiteral.getPosition());
	}


	private Collection<Token> getPrecedingTokens(Transition transition) {
		// TODO Auto-generated method stub
		return null;
	}


	private final class SearchState {
		private Token token;
		private int charOffset;
//		private int charLength;
		private boolean inside;
		private Transition transition;
	}

	private SearchState findCurrentToken(State state, final IAbstractString currentLiteral, final int offset) {

		final SearchState closest = new SearchState();
		
		Queue<State> queue = new LinkedList<State>();
		queue.offer(state);
		Set<State> visited = new HashSet<State>();
		visited.add(state);
		while (!queue.isEmpty()) {
			State s = queue.poll();
			for (final Transition transition : s.getOutgoingTransitions()) {
				final Token token = (Token) transition.getInChar();
				final boolean[] first = new boolean[] {true};
				token.getText().fold(null, new IFoldFunction<Void, IAbstractInputItem>() {

					@Override
					public Void body(Void init, IAbstractInputItem arg,	boolean last) {
						PositionedCharacter c = (PositionedCharacter) arg;
						IPosition stringPos = c.getStringPosition();
						if (stringPos.equals(currentLiteral.getPosition())) {
							int strStart = stringPos.getStart();
							int offs = strStart + c.getIndexInString();
							int len = c.getLengthInSource();
							if (offset < offs + len) {
								if (closest.token == null
										|| offs < closest.charOffset) {
									closest.token = token;
									closest.transition = transition;
									closest.charOffset = offs;
//									closest.charLength = len;
									closest.inside = !first[0];
								}
							}
						}
						first[0] = false;
						return null;
					}
				});
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
}
