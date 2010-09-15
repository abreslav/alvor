package com.zeroturnaround.alvor.lexer.automata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Initializes incoming transitions in automata.
 * 
 * By default, incoming transitions are not stored (they are rarely needed and occupy memory),
 * if one needs to use "backlinks" in automata or transducers, they need to initialize them by 
 * using this class.
 * 
 * @author abreslav
 *
 */
public class IncomingTransitionsInitializer {

	/**
	 * Fills in incomingTransitions collections in all {@link State}s reachable from the given one.
	 * @param state the state to strat with
	 */
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
