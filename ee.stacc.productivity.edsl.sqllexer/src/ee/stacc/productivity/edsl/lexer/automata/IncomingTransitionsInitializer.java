package ee.stacc.productivity.edsl.lexer.automata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncomingTransitionsInitializer {

	public static void initializeIncomingTransitions(State state) {
		Set<State> visited = new HashSet<State>();
		List<State> stack = new ArrayList<State>();
		state.initializeIncomingTransitions();
		stack.add(state);
		visited.add(state);
		while (!stack.isEmpty()) {
			State current = stack.remove(stack.size() - 1);
			for (Transition transition : current.getOutgoingTransitions()) {
				State to = transition.getTo();
				to.initializeIncomingTransitions();
				to.addIncoming(transition);
				if (!visited.contains(to)) {
					stack.add(to);
					visited.add(to);
				}
			}
		}
	}
}
