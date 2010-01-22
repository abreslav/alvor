package ee.stacc.productivity.edsl.lexer.automata;

import java.util.LinkedHashSet;
import java.util.Set;

import ee.stacc.productivity.edsl.sqllexer.SQLLexerData;

public class AutomataUtils {

	public interface ICharacterMapper {
		String map(int c);
	}

	public static final ICharacterMapper SQL_TOKEN_MAPPER = new ICharacterMapper() {
		@Override
		public String map(int c) {
			return SQLLexerData.TOKENS[c];
		}
	};
	
	public static final ICharacterMapper SQL_IN_MAPPER = new ICharacterMapper() {
		@Override
		public String map(int c) {
			char[] cc = SQLLexerData.CHAR_CLASSES;
			for (int i = 0; i < cc.length; i++) {
				if (cc[i] == c) {
					if (Character.isWhitespace((char) i)) {
						return "WS";
					} if (i == 0) {
						return "'\\0'";
					} else { 
						return "'" + ((char) i) + "'";
					}
				}
			}
			throw new IllegalStateException("Impossible state");
		}
	};

	public static final ICharacterMapper ID_MAPPER = new ICharacterMapper() {
		@Override
		public String map(int c) {
			return "" + (char) c;
		}
	};
	
	public static void printAutomaton(State initial) {
		printAutomaton(initial, ID_MAPPER, ID_MAPPER);
	}
	
	public static void printSQLAutomaton(State initial) {
		printAutomaton(initial, ID_MAPPER, SQL_TOKEN_MAPPER);
	}
	
	public static void printAutomaton(State initial, ICharacterMapper inMapper, ICharacterMapper outMapper) {
		LinkedHashSet<State> states = new LinkedHashSet<State>();
		EmptyTransitionEliminator.INSTANCE.dfs(initial, states);
		System.out.println(statesToString(states, initial, inMapper, outMapper));
		System.out.println();
	}
	
	public static State toAutomaton(Set<String> strings) {
		State initial = new State("START", strings.contains(""));
		for (String string : strings) {
			generateChain(initial, string);
		}
		return AutomataDeterminator.determinate(initial);
	}

	private static void generateChain(State initial, String string) {
		State current = initial;
		for (int i = 0; i < string.length(); i++) {
			char charAt = string.charAt(i);
			State state = new State("" + charAt, false);
			current.getOutgoingTransitions().add(
					new Transition(current, state, (int) charAt)	
			);
			current = state;
		}
		current.getOutgoingTransitions().add(
				new Transition(current, new State("EOF", true), -1)	
		);
		
	}
	
	public static String statesToString(Set<State> theStates,
			State theInitialState, ICharacterMapper inMapper, ICharacterMapper outMapper) {
		StringBuilder stringBuilder = new StringBuilder();
		for (State state : theStates) {
			if (theInitialState == state) {
				stringBuilder.append("->");
			}
			if (state.isAccepting()) {
				stringBuilder.append("a:");
			}
			stringBuilder
				.append(state);
			if (!state.getOutgoingTransitions().isEmpty()) {
				stringBuilder.append(" -> ");
			}
			for (Transition transition : state.getOutgoingTransitions()) {
				int inChar = transition.getInChar();
				String inStr;
				if (inChar == -1) {
					inStr = "EOF";
				} else {
					inStr = inChar == 0 ? "" : inMapper.map(inChar) + "";
				}
				stringBuilder
					.append(transition.getTo())
					.append(":")
					.append(inStr)
					.append("/")
					.append(mapString(transition.getOutStr(), outMapper))
					.append("/")
					.append(" ");
			}
			stringBuilder.append(";\n");
		}
		return stringBuilder.toString();
	}
	
	private static String mapString(String s, ICharacterMapper mapper) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			result.append(mapper.map(s.charAt(i)));
		}
		return result.toString();
	}
}
