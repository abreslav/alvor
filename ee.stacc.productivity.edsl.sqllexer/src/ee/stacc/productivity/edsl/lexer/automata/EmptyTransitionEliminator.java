package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class EmptyTransitionEliminator {

	public static final EmptyTransitionEliminator INSTANCE = new EmptyTransitionEliminator();
	
	private EmptyTransitionEliminator() {}
	
	/**
	 * This \epsilon-closes the automaton
	 */
	public State eliminateEmptySetTransitions(State initial) {
		Set<State> states = new LinkedHashSet<State>();
		dfs(initial, states);
		
		Map<State, Set<State>> stateSets = new HashMap<State, Set<State>>();
		Set<State> accepting = new HashSet<State>();
		for (State state : states) {
			Set<State> reachableByEmpty = new HashSet<State>();
			if (close(state, state, reachableByEmpty)) {
				accepting.add(state);
			}
			stateSets.put(state, reachableByEmpty);
		}
		
		Map<State, State> newStates = new HashMap<State, State>();
		for (State oldState : states) {
			State newState = getNewState(newStates, oldState, accepting);
			Set<State> reachableByEmpty = stateSets.get(oldState);
			for (State state : reachableByEmpty) {
				for (Transition oldTransition : state.getOutgoingTransitions()) {
					if (!oldTransition.getInSet().isEmpty()) {
						newState.getOutgoingTransitions().add(
								new Transition(
										newState, 
										getNewState(
												newStates, 
												oldTransition.getTo(), 
												accepting),
												oldTransition.getInSet()
								));
					}
				}
			}
			
		}
		
		return newStates.get(initial);
	}


	private State getNewState(Map<State, State> newStates,
			State oldState, Set<State> accepting) {
		State newState = newStates.get(oldState);
		if (newState == null) {
			newState = new State(oldState.getName() + "'", accepting.contains(oldState));
			newStates.put(oldState, newState);
		}
		return newState;
	}
	
	public void dfs(State start, Set<State> visited) {
		if (visited.contains(start)) {
			return;
		}
		
		visited.add(start);
		
		Collection<Transition> outgoingTransitions = start.getOutgoingTransitions();
		for (Transition transition : outgoingTransitions) {
			dfs(transition.getTo(), visited);
		}
	}


	private boolean close(State state, State rep, Set<State> visited) {
		if (visited.contains(state)) {
			return state.isAccepting();
		}
		visited.add(state);
		Collection<Transition> outgoingTransitions = state.getOutgoingTransitions();
		boolean result = state.isAccepting();
		for (Transition transition : outgoingTransitions) {
			if (transition.getInSet().isEmpty()) {
				if (close(transition.getTo(), rep, visited)) {
					result = true;
				}
			}
		}
		return result;
	}
	
}
