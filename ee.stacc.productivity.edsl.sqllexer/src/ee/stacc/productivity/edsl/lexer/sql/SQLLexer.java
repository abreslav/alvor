package ee.stacc.productivity.edsl.lexer.sql;

import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.ACTIONS;
import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.ATTRIBUTES;
import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.CHAR_CLASS_COUNT;
import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.STATE_COUNT;
import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.TRANSITIONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractOutputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence;
import ee.stacc.productivity.edsl.lexer.alphabet.PushInput;
import ee.stacc.productivity.edsl.lexer.alphabet.SequenceUtil;
import ee.stacc.productivity.edsl.lexer.alphabet.SimpleCharacter;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.alphabet.Yield;
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

	private static final int ID_CODE;
	private static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(SQLLexerData.KEYWORDS));
	private static final Map<String, Integer> TOKEN_NAME_TO_CODE = new HashMap<String, Integer>();
	static {
		TOKEN_NAME_TO_CODE.put("EOF", -1);
		int nonKeywordCount = SQLLexerData.TOKENS.length;
		for (int i = 0; i < nonKeywordCount; i++) {
			TOKEN_NAME_TO_CODE.put(SQLLexerData.TOKENS[i], i);
		}
		for (int i = 0; i < SQLLexerData.KEYWORDS.length; i++) {
			TOKEN_NAME_TO_CODE.put(SQLLexerData.KEYWORDS[i], nonKeywordCount + i);
		}
		ID_CODE = getCodeByName("ID");
	}
	
	public static int getCodeByName(String name) {
		Integer code = TOKEN_NAME_TO_CODE.get(name);
		if (code == null) {
			throw new IllegalArgumentException("Unknown token type: " + name);
		}
		return code;
	}
	
	public static boolean isWhitespace(int code) {
		return getTokenName(code).length() == 0;	
	}
	
	public static boolean isIdentifier(int code) {
		return "ID".equals(getTokenName(code));	
	}
	
	public static String getTokenName(int c) {
		if (c == -1) {
			return "EOF";
		}
		int nonKeywordCount = SQLLexerData.TOKENS.length;
		if (c < nonKeywordCount) {
			return SQLLexerData.TOKENS[c];
		}
		return SQLLexerData.KEYWORDS[c - nonKeywordCount];
	}
	
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
						List<IAbstractOutputItem> actionList = new ArrayList<IAbstractOutputItem>();
						if (isImmediatelyGenerating(toIndex)) {
							int actionIndex = ACTIONS[toIndex];
							if (actionIndex >= 0) {
								actionList.add(PushInput.INSTANCE);
								actionList.add(Yield.create(actionIndex));
							} else {
								throw new IllegalStateException();
							}
						} else {
							actionList.add(PushInput.INSTANCE);
						}
						Transition.create(from, states[toIndex], 
									SimpleCharacter.create(Integer.valueOf(cc)), 
									actionList);
					}
				}
				if (isAccepting(fromIndex)) {
					acceptingStatesIndices.add(fromIndex);
				}
			}

			Transition.create(
					initialState, EOFState, 
					IAbstractInputItem.EOF, 
					Collections.singletonList(Yield.create(-1))
			);
			
			// Make it circular
			// Imagine an \eps-transition into initialState from every accepting state
			// for immediately generating states these do not produce any output
			// for others they produce some output
			// eliminating these imaginary transitions gives us an automaton which recognizes 
			// a token stream
			Iterable<Transition> initialTransitions = initialState.getOutgoingTransitions();
			for (Integer stateIndex : acceptingStatesIndices) {
				State state = states[stateIndex];
				for (Transition transition : initialTransitions) {
					State to = transition.getTo();
					IAbstractInputItem inChar = transition.getInChar();
					List<IAbstractOutputItem> initialOutStr = transition.getOutput();
					List<IAbstractOutputItem> resultingOutStr = isImmediatelyGenerating(stateIndex) 
						? initialOutStr 
						: prepend(Yield.create(ACTIONS[stateIndex]), initialOutStr);
					Transition.create(state, to, inChar, resultingOutStr);
				}
			}
			
			AutomataDeterminator.determinateWithPriorities(initialState);
			return initialState;
		}
		
		private boolean isAccepting(int state) {
			return (ATTRIBUTES[state] & 0001) != 0;
		}

		private boolean isImmediatelyGenerating(int state) {
			return (ATTRIBUTES[state] & 0010) != 0;
		}
	}
	
	private static <T> List<T> prepend(T item, List<? extends T> list) {
		ArrayList<T> result = new ArrayList<T>();
		result.add(item);
		result.addAll(list);
		return result;
	}
	
	
	public static String tokenToString(Token token) {
		String tokenName = SQLLexer.getTokenName(token.getType());
		ISequence<IAbstractInputItem> text = token.getText();
		String textToStr = SequenceUtil.toString(text);
		return tokenName + "[" + textToStr + "]";
	}

	/**
	 * @param id -- token text
	 * @return keyword token if id is a keyword or the code for ID token otherwise 
	 */
	public static int getIdentifierTokenType(String id) {
		String upperCase = id.toUpperCase();
		if (KEYWORDS.contains(upperCase)) {
			return getCodeByName(upperCase);
		} 
		return ID_CODE; 
	}
}
