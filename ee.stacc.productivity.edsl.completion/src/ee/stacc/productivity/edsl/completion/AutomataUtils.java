package ee.stacc.productivity.edsl.completion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ee.stacc.productivity.edsl.completion.ContextRecognizer.ITraversalStrategy;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.SimpleCharacter;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.automata.AutomataDeterminator;
import ee.stacc.productivity.edsl.lexer.automata.EmptyTransitionEliminator;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexer;

public class AutomataUtils {

	public static final ICharacterMapper SQL_TOKEN_MAPPER = new AbstractCharacterMapper() {
		@Override
		public String map(int c) {
			return SQLLexer.getTokenName(c);
		}
	};
	
//	public static final ICharacterMapper SQL_IN_MAPPER = new AbstractCharacterMapper() {
//		@Override
//		public String map(int c) {
//			if (c == (char) -1) {
//				return "EOF";
//			}
//			char[] cc = SQLLexerData.CHAR_CLASSES;
//			for (int i = 0; i < cc.length; i++) {
//				if (cc[i] == c) {
//					if (Character.isWhitespace((char) i)) {
//						return "WS";
//					} if (i == 0) {
//						return "'\\0'";
//					} else { 
//						return "'" + ((char) i) + "'";
//					}
//				}
//			}
//			throw new IllegalStateException("Impossible state: '" + ((int) c) + "'");
//		}
//	};

	public static final ICharacterMapper ID_MAPPER = new AbstractCharacterMapper() {
		@Override
		public String map(int c) {
			return "" + (char) c;
		}
	};
	
	public static void printAutomaton(State initial) {
		printAutomaton(initial, ID_MAPPER, ID_MAPPER, ITraversalStrategy.FORWARD);
	}
	
	public static void printSQLAutomaton(State initial) {
		printAutomaton(initial, ID_MAPPER, SQL_TOKEN_MAPPER, ITraversalStrategy.FORWARD);
	}
	
	public static void printSQLInputAutomaton(State initial) {
		printAutomaton(initial, SQL_TOKEN_MAPPER, ID_MAPPER, ITraversalStrategy.FORWARD);
	}
	
	public static void printSQLInputAutomatonBackward(State initial) {
		printAutomaton(initial, SQL_TOKEN_MAPPER, ID_MAPPER, ITraversalStrategy.BACKWARD);
	}
	
	public static void printAutomaton(State initial, ICharacterMapper inMapper, ICharacterMapper outMapper, ITraversalStrategy strategy) {
		LinkedHashSet<State> states = new LinkedHashSet<State>();
		EmptyTransitionEliminator.INSTANCE.dfs(initial, states);
		System.out.println(statesToString(states, initial, inMapper, outMapper, new NumbererRenderer(), strategy));
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
			Transition.create(current, state, SimpleCharacter.create(charAt));	
			current = state;
		}
		Transition.create(current, new State("EOF", true), IAbstractInputItem.EOF);	
		
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
			IStateRenderer renderer, ITraversalStrategy strategy) {
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
			
			if (strategy.getTransitions(state).iterator().hasNext()) {
				stringBuilder.append(" -> ");
			}
			for (Transition transition : strategy.getTransitions(state)) {
				String inStr;
				if (!transition.isEmpty()) {
					int inChar = transition.getInChar().getCode();
					if (inChar == -1) {
						inStr = "EOF";
					} else {
						inStr = inChar == 0 ? "" : inMapper.map(inChar) + "";
					}
				} else {
					inStr = "\\eps";
				}
				stringBuilder
					.append(renderer.render(strategy.getTarget(transition)))
					.append(":")
					.append(inStr)
					.append("/")
					.append(transition.getOutput())
//					.append(mapString(transition.getOutput(), outMapper))
					.append("/")
					.append(" ");
			}
			stringBuilder.append(";\n");
		}
		return stringBuilder.toString();
	}
//	
//	private static String mapString(String s, ICharacterMapper mapper) {
//		StringBuilder result = new StringBuilder();
//		for (int i = 0; i < s.length(); i++) {
//			result.append(mapper.map(s.charAt(i)));
//		}
//		return result.toString();
//	}

	
	public static void generate(State state) {
		generate(state, ID_MAPPER, STANDARD_OUTPUT);
	}
	
	public interface IOutput {
		void putString(String str);
	}
	
	public static final IOutput STANDARD_OUTPUT = new IOutput() {

		@Override
		public void putString(String str) {
			System.out.println(str);
		}
		
	};

	public static final IInputToString TO_STRING = new IInputToString() {
			
		@Override
		public String toString(IAbstractInputItem item) {
			return item.toString();
		}
	};

	public static final IInputToString SQL_TOKEN_TO_STRING = new IInputToString() {
		
		@Override
		public String toString(IAbstractInputItem item) {
			Token token = (Token) item;
			return SQLLexer.tokenToString(token);
		}
	};
	
	public static void generate(State state, IInputToString toStr, IOutput output) {
		Set<Transition> visitedTransitions = new HashSet<Transition>();
		Iterable<Transition> outgoingTransitions = state.getOutgoingTransitions();
		for (Transition transition : outgoingTransitions) {
			doGenerate(transition, "", toStr, visitedTransitions, output);
		}
	}

	private static void doGenerate(Transition tr, String out, 
			IInputToString toStr, Set<Transition> visitedTransitions, 
			IOutput output) {
		if (!visitedTransitions.add(tr)) {
			return;
		}
		State to = tr.getTo();
		out += toStr.toString(tr.getInChar()) + " ";
		if (to.isAccepting()) {
			output.putString(out);
		}
		for (Transition transition : to.getOutgoingTransitions()) {
			doGenerate(transition, out, toStr, visitedTransitions, output);
		}
		visitedTransitions.remove(tr);
	}
}
