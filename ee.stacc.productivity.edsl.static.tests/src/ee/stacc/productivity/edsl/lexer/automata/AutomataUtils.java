package ee.stacc.productivity.edsl.lexer.automata;

import java.util.LinkedHashSet;
import java.util.Set;

import ee.stacc.productivity.edsl.sqllexer.SQLLexerData;

public class AutomataUtils {
	public static void printAutomaton(State transduction) {
		LinkedHashSet<State> states = new LinkedHashSet<State>();
		EmptyTransitionEliminator.INSTANCE.dfs(transduction, states);
		System.out.println(AutomataParser.statesToString(states, transduction));
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
					new Transition(current, state, (int) SQLLexerData.CHAR_CLASSES[charAt])	
			);
			current = state;
		}
		current.getOutgoingTransitions().add(
				new Transition(current, new State("EOF", true), -1)	
		);
		
	}
}
