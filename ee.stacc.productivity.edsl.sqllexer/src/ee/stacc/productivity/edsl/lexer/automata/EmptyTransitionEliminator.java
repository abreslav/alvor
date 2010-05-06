package ee.stacc.productivity.edsl.lexer.automata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class EmptyTransitionEliminator {

	public interface IEmptinessExpert {
		IEmptinessExpert DEFAULT = new IEmptinessExpert() {
			
			@Override
			public boolean isEmpty(Transition transition) {
				return transition.isEmpty();
			}
		};
		
		boolean isEmpty(Transition transition);
	}
	
	
	public static final EmptyTransitionEliminator INSTANCE = new EmptyTransitionEliminator();
	
	private EmptyTransitionEliminator() {}
	
	public State eliminateEmptySetTransitions(State initial) {
		return eliminateEmptySetTransitions(initial, IEmptinessExpert.DEFAULT);
	}
	
	/**
	 * This \epsilon-closes the transducer
	 */
	public State eliminateEmptySetTransitions(State initial, IEmptinessExpert emptinessExpert) {
		// Collect reachable states
		Set<State> states = new LinkedHashSet<State>();
		dfs(initial, states);
		
		// Find what states are reachable from each one by only empty transitions
		Map<State, Set<State>> stateSets = new HashMap<State, Set<State>>();
		Set<State> accepting = new HashSet<State>();
		for (State state : states) {
			Set<State> reachableByEmpty = new HashSet<State>();
			if (close(state, state, reachableByEmpty, emptinessExpert)) {
				accepting.add(state);
			}
			stateSets.put(state, reachableByEmpty);
		}
		
		// Create the new transducer
		Map<State, State> newStates = new HashMap<State, State>();
		for (State oldState : states) {
			State newState = getNewState(newStates, oldState, accepting);
			Set<State> reachableByEmpty = stateSets.get(oldState);
			for (State state : reachableByEmpty) {
				for (Transition oldTransition : state.getOutgoingTransitions()) {
					if (!emptinessExpert.isEmpty(oldTransition)) {
							Transition.create(
									newState, 
									getNewState(
											newStates, 
											oldTransition.getTo(), 
											accepting),
									oldTransition.getInChar(),
									oldTransition.getOutput()
							);
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
			// ' was appended here
			newState = new State(oldState.getName(), accepting.contains(oldState));
			newStates.put(oldState, newState);
		}
		return newState;
	}
	
	public void dfs(State start, Set<State> visited) {
		if (visited.contains(start)) {
			return;
		}
		
		visited.add(start);
		
		for (Transition transition : start.getOutgoingTransitions()) {
			dfs(transition.getTo(), visited);
		}
	}


	private boolean close(State state, State rep, Set<State> visited, IEmptinessExpert emptinessExpert) {
		if (visited.contains(state)) {
			return state.isAccepting();
		}
		visited.add(state);
		boolean result = state.isAccepting();
		for (Transition transition : state.getOutgoingTransitions()) {
			if (emptinessExpert.isEmpty(transition)) {
				if (close(transition.getTo(), rep, visited, emptinessExpert)) {
					result = true;
				}
			}
		}
		return result;
	}
//
//	public void deleteEmptyTransitionsPreservingConnectivity(State initial) {
//		HashSet<State> states = new HashSet<State>();
//		dfs(initial, states);
//		Set<Transition> emptyTransitions = new HashSet<Transition>();
//		for (State state : states) {
//			emptyTransitions.clear();
//			Collection<Transition> outgoingTransitions = state.getOutgoingTransitions();
//			boolean nonEmptyFound = false;
//			for (Transition transition : outgoingTransitions) {
//				if (transition.isEmpty()) {
//					emptyTransitions.add(transition);
//				} else {
//					nonEmptyFound = true;
//				}
//			}
//			if (nonEmptyFound) {
//				outgoingTransitions.removeAll(emptyTransitions);
//			} else {
//			}
//		}
//	}
}
