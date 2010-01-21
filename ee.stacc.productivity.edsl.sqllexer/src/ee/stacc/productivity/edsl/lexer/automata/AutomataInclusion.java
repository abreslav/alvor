package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AutomataInclusion {

	public static final AutomataInclusion INSTANCE = new AutomataInclusion();
	
	private AutomataInclusion() {}
	
	/**
	 * Check if the first contains the second
	 */
	public boolean checkInclusion(State initial, State start) {
		return new Checker().check(initial, start);
	}
	
	/**
	 * Transduce the second by the first
	 * @return the resulting automaton's initial state
	 */
	public State getTrasduction(State transducerInitial, State inputInitial) {
		Checker checker = new Checker();
		if (!checker.check(transducerInitial, inputInitial)) {
			throw new IllegalArgumentException("The given automaton does not form a valid input for the given transducer");
		}
		Map<Transition, Set<Transition>> transitionMap = checker.transitionMap;
		Map<State, State> oldToNewStates = new HashMap<State, State>();
		Set<State> oldStates = checker.stateMap.keySet();
		for (State oldState : oldStates) {
			State newState = getNewState(oldToNewStates, oldState);
			Collection<Transition> oldTransitions = oldState.getOutgoingTransitions();
			for (Transition oldTransition : oldTransitions) {
				Set<Transition> transducerTransitions = getSet(transitionMap, oldTransition);
				for (Transition transducerTransition : transducerTransitions) {
					String outStr = transducerTransition.getOutStr();
					State newTo = getNewState(oldToNewStates, oldTransition.getTo());
					int length = outStr.length();
					switch (length) {
					case 0:
						createTransition(newState, newTo, null);
						break;
					case 1:
						createTransition(newState, newTo, (int) outStr.charAt(0));
						break;
					default:
						State current = newState;
						for (int i = 0; i < length; i++) {
							State state;
							if (i == length) {
								state = newTo;
							} else {
								state = new State("I" + i, false);
							}
							createTransition(current, state, (int) outStr.charAt(i));
							current = state;
						}
						break;
					}
				}
			}
		}
		return oldToNewStates.get(inputInitial);
	}

	private void createTransition(State from, State to, Integer c) {
		from.getOutgoingTransitions().add(
				new Transition(
						from, 
						to, 
						c));
	}

	private State getNewState(Map<State, State> oldToNewStates, State oldState) {
		State newState = oldToNewStates.get(oldState);
		if (newState == null) {
			newState = new State(oldState.getName() + "'", oldState.isAccepting());
			oldToNewStates.put(oldState, newState);
		}
		return newState;
	}
	
	private static class Checker {

		/**
		 * Maps a state of the included automaton to a set of states of including automaton  
		 */
		private final Map<State, Set<State>> stateMap = new HashMap<State, Set<State>>();
		/**
		 * Maps a transition of the included automaton to a set of transitions of including automaton  
		 */
		private final Map<Transition, Set<Transition>> transitionMap = new HashMap<Transition, Set<Transition>>();

		private final State error = new State("<ERROR>", false);
		
		public boolean check(State initial, State start) {
			return dfs(start, Collections.singleton(initial));
		}
		
		private boolean dfs(State current, Set<State> incoming) {
			Set<State> setForCurrent = getSet(stateMap, current);
			if (setForCurrent.containsAll(incoming)) {
				return true;
			}
			
			setForCurrent.addAll(incoming);
			if (setForCurrent.contains(error)) {
				return false;
			}
			
			if (current.isAccepting()) {
				for (State state : setForCurrent) {
					if (!state.isAccepting()) {
						return false;
					}
				}
			}
			
			Collection<Transition> transitions = current.getOutgoingTransitions();
			for (Transition transition : transitions) {
				Set<State> newSet = transitionFunction(
						setForCurrent, 
						error, 
						transition);
				boolean result = dfs(
					transition.getTo(), 
					newSet);
				if (!result) {
					return false;
				}
			}
			return true;
		}
		
		private Set<State> transitionFunction(State state, State error, Transition underlyingTransition) {
			Set<State> result = new HashSet<State>();
			if (underlyingTransition.isEmpty()) {
				result.add(error);
				return result;
			}
			
			Collection<Transition> transitions = state.getOutgoingTransitions();
			Set<Transition> correspondingTransitions = getSet(transitionMap, underlyingTransition);
			int inChar = underlyingTransition.getInChar();
			boolean anyTransition = false;
			for (Transition transition : transitions) {
				if (transition.getInChar() == inChar) {
					result.add(transition.getTo());
					correspondingTransitions.add(transition);
					anyTransition = true;
				}
			}
			if (!anyTransition) {
				result.add(error);
			}
			return result;
		}
		
		public Set<State> transitionFunction(Set<State> states, State error, Transition underlyingTransition) {
			Set<State> result = new HashSet<State>();
			for (State state : states) {
				result.addAll(transitionFunction(state, error, underlyingTransition));
			}	
			return result;
		}

	}

	public static <K, V> Set<V> getSet(Map<K, Set<V>> map, K key) {
		Set<V> set = map.get(key);
		if (set == null) {
			set = new HashSet<V>();
			map.put(key, set);
		}
		return set;
	}
	
}
