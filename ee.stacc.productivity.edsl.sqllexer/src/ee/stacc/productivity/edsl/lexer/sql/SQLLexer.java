package ee.stacc.productivity.edsl.lexer.sql;

import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.ACTIONS;
import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.ATTRIBUTES;
import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.CHAR_CLASS_COUNT;
import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.STATE_COUNT;
import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.TRANSITIONS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ee.stacc.productivity.edsl.lexer.automata.AutomataDeterminator;
import ee.stacc.productivity.edsl.lexer.automata.IAlphabetConverter;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.sqllexer.SQLLexerData;

public class SQLLexer {

	public static final IAlphabetConverter SQL_ALPHABET_CONVERTER = new IAlphabetConverter() {
		@Override
		public int convert(int c) {
			if (c == -1) {
				return c;
			}
			return SQLLexerData.CHAR_CLASSES[c];
		}
	};

	public static final State SQL_TRANSDUCER = new AutomataConverter().convert();

	private static class AutomataConverter {

		public State convert() {
			// Create states
			State[] states = new State[STATE_COUNT + 1];
			for (int i = 0; i < STATE_COUNT; i++) {
				states[i] = new State("S" + i, false);
			}
			State initialState = states[0];
			State EOFState = new State("EOF", true);
			states[STATE_COUNT] = EOFState;
			
			// Create transitions (for recognizing a single token
			List<Integer> acceptingStatesIndices = new ArrayList<Integer>();
			for (int fromIndex = 0; fromIndex < STATE_COUNT; fromIndex++) {
				State from = states[fromIndex];
				for (char cc = 0; cc < CHAR_CLASS_COUNT; cc++) {
					int toIndex = TRANSITIONS[fromIndex][cc];
					if (toIndex != -1) {
						String out = "";
						if (isImmediatelyGenerating(toIndex)) {
							int action = ACTIONS[toIndex];
						    out = action >= 0 ? "" + (char) action : "";
						}
						Transition transition = new Transition(from, states[toIndex], Integer.valueOf(cc), out);
						from.getOutgoingTransitions().add(transition);
					}
				}
				if (isAccepting(fromIndex)) {
					acceptingStatesIndices.add(fromIndex);
				}
			}

			initialState.getOutgoingTransitions().add(new Transition(initialState, EOFState, -1, "" + (char) -1));
			
			// Make it circular
			// Imagine an \eps-transition into initialState from every accepting state
			// for immediately generating states these do not produce any output
			// for others they produce some output
			// eliminating these imaginary transitions gives us an automaton which recognizes 
			// a token stream
			Collection<Transition> initialTransitions = initialState.getOutgoingTransitions();
			for (Integer stateIndex : acceptingStatesIndices) {
				State state = states[stateIndex];
				Collection<Transition> outgoingTransitions = state.getOutgoingTransitions();
				for (Transition transition : initialTransitions) {
					State to = transition.getTo();
					int inChar = transition.getInChar();
					String initialOutStr = transition.getOutStr();
					String resultingOutStr = isImmediatelyGenerating(stateIndex) 
						? initialOutStr 
						: (char) ACTIONS[stateIndex] + initialOutStr;
					outgoingTransitions.add(new Transition(state, to, inChar, resultingOutStr));
				}
			}
			
			return AutomataDeterminator.determinateWithPriorities(initialState);
		}
		
		private boolean isAccepting(int state) {
			return (ATTRIBUTES[state] & 0001) != 0;
		}

		private boolean isImmediatelyGenerating(int state) {
			return (ATTRIBUTES[state] & 0010) != 0;
		}
	}
	
}
