package ee.stacc.productivity.edsl.lexer.automata;

import java.util.LinkedHashSet;

public class AutomataUtils {
	public static void printAutomaton(State transduction) {
		LinkedHashSet<State> states = new LinkedHashSet<State>();
		EmptyTransitionEliminator.INSTANCE.dfs(transduction, states);
		System.out.println(AutomataParser.statesToString(states, transduction));
	}
}
