package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ee.stacc.productivity.edsl.sqllexer.SQLLexerData;

public class AutomataUtils {

	public static final ICharacterMapper SQL_TOKEN_MAPPER = new ICharacterMapper() {
		@Override
		public String map(int c) {
			if (c == (char) -1) {
				return "EOF";
			}
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
	
	public static void printSQLInputAutomaton(State initial) {
		printAutomaton(initial, SQL_TOKEN_MAPPER, ID_MAPPER);
	}
	
	public static void printAutomaton(State initial, ICharacterMapper inMapper, ICharacterMapper outMapper) {
		LinkedHashSet<State> states = new LinkedHashSet<State>();
		EmptyTransitionEliminator.INSTANCE.dfs(initial, states);
		System.out.println(statesToString(states, initial, inMapper, outMapper, new NumbererRenderer()));
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
	
	public interface IStateRenderer {
		String render(State state);
	}
	
	public static final class NumbererRenderer implements IStateRenderer {

		private int count;
		private final Map<State, String> nameMap = new HashMap<State, String>(); 
		
		@Override
		public String render(State state) {
			String name = nameMap.get(state);
			if (name == null) {
				name = "S" + count;
				count++;
				nameMap.put(state, name);
			}
			return name;
		}
		
	};
	
	public static final IStateRenderer ID = new IStateRenderer() {
		
		@Override
		public String render(State state) {
			return state.toString();
		}
	};
	
	public static String statesToString(Set<State> theStates, State theInitialState, 
			ICharacterMapper inMapper, ICharacterMapper outMapper,
			IStateRenderer renderer) {
		StringBuilder stringBuilder = new StringBuilder();
		for (State state : theStates) {
			if (theInitialState == state) {
				stringBuilder.append("->");
			}
			if (state.isAccepting()) {
				stringBuilder.append("a:");
			}
			stringBuilder
				.append(renderer.render(state));
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
					.append(renderer.render(transition.getTo()))
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
	
	public static void generate(State state, String out) {
		generate(state, out, ID_MAPPER);
	}
	
	public static void generate(State state, String out, ICharacterMapper outputMapper) {
		Set<Transition> visitedTransitions = new HashSet<Transition>();
//		HashMap<State, Integer> visitCounts = new HashMap<State, Integer>();
//		int maxVisits = 2;
//		doGenerate(state, out, outputMapper, visitCounts, maxVisits, visitedTransitions);
		Collection<Transition> outgoingTransitions = state.getOutgoingTransitions();
		for (Transition transition : outgoingTransitions) {
			doGenerate(transition, out, outputMapper, visitedTransitions);
		}
	}

	private static void doGenerate(Transition tr, String out, ICharacterMapper outputMapper, Set<Transition> visitedTransitions) {
		if (!visitedTransitions.add(tr)) {
			return;
		}
		State to = tr.getTo();
		out += outputMapper.map(tr.getInChar()) + " ";
		if (to.isAccepting()) {
			System.out.println(out);
		}
		for (Transition transition : to.getOutgoingTransitions()) {
			doGenerate(transition, out, outputMapper, visitedTransitions);
		}
		visitedTransitions.remove(tr);
	}
	
//	private static void doGenerate(State state, String out,
//			ICharacterMapper outputMapper, HashMap<State, Integer> visitCounts,
//			int maxVisits, Set<Transition> visitedTransitions) {
//		Integer count = visitCounts.get(state);
//		if (count == null) {
//			count = 0;
//		}
//		if (count >= maxVisits) {
//			return;
//		}
//		visitCounts.put(state, count + 1);
//		if (state.isAccepting()) {
//			System.out.println(out);
//		}
//		Collection<Transition> outgoingTransitions = state.getOutgoingTransitions();
//		for (Transition transition : outgoingTransitions) {
//			if (!visitedTransitions.add(transition)) {
////				continue;
//			}
//			doGenerate(transition.getTo(), out + outputMapper.map(transition.getInChar()) + " ", outputMapper,
//					visitCounts, maxVisits, visitedTransitions);
//		}
//	}	
}
