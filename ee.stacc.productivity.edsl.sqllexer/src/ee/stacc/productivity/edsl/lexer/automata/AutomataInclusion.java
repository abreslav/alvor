package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
			println("oldState: " + oldState + System.identityHashCode(oldState));
			State newState = getNewState(oldToNewStates, oldState);
			println("newState: " + newState + System.identityHashCode(newState));
			Collection<Transition> oldTransitions = oldState.getOutgoingTransitions();
			for (Transition oldTransition : oldTransitions) {
				println("  oldTransition: " + oldTransition);
				Set<Transition> transducerTransitions = getSet(transitionMap, oldTransition);
				for (Transition transducerTransition : transducerTransitions) {
					println("  transducerTransition: " + transducerTransition);
					String outStr = transducerTransition.getOutStr();
					State newTo = getNewState(oldToNewStates, oldTransition.getTo());
					println("  newTo: " + newTo + System.identityHashCode(newTo));
					
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
							if (i == length - 1) {
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

	private void println(String string) {
//		System.out.println(string);
	}

	private void createTransition(State from, State to, Integer c) {
		Transition transition = new Transition(from, to, c);
		println("    newTransition: " + transition);
		from.getOutgoingTransitions().add(transition);
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
		private final Map<State, Set<State>> stateMap = new LinkedHashMap<State, Set<State>>();
		/**
		 * Maps a transition of the included automaton to a set of transitions of including automaton  
		 */
		private final Map<Transition, Set<Transition>> transitionMap = new LinkedHashMap<Transition, Set<Transition>>();

		private final State error = new State("<ERROR>", false);
		
		public boolean check(State initial, State start) {
			getSet(stateMap, start).add(initial);
			return dfs(start
//					, Collections.singleton(initial)
					);
		}
		
		private boolean dfs(State current
//				, Set<State> incoming
				) {
			Set<State> setForCurrent = getSet(stateMap, current);
//			if (setForCurrent.containsAll(incoming)) {
//				return true;
//			}
			
//			setForCurrent.addAll(incoming);
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
				Change status = transitionFunction(new HashSet<State>(setForCurrent), error, transition);
//				Set<State> newSet = transitionFunction(setForCurrent, error, transition);
				switch (status) {
				case NONE:
					break;
				case SOME:
					boolean result = dfs(transition.getTo()
//						, newSet
					);
					if (!result) {
						return false;
					}
					break;
				case ERROR:
					return false;
				}
			}
			return true;
		}
	
		private static enum Change {
			NONE,
			SOME,
			ERROR
		}
		
		private 
//		Set<State>
		Change
		transitionFunction(State state, State error, Transition underlyingTransition) {
			Set<State> correspondingStates = getSet(stateMap, underlyingTransition.getTo());
//			Set<State> result = new HashSet<State>();
			if (underlyingTransition.isEmpty()) {
				correspondingStates.add(error);
				return Change.ERROR;
//				result.add(error);
//				return result;
			}
			
			Change result = Change.NONE;
			Collection<Transition> transitions = state.getOutgoingTransitions();
			Set<Transition> correspondingTransitions = getSet(transitionMap, underlyingTransition);
			int inChar = underlyingTransition.getInChar();
			boolean anyTransition = false;
			for (Transition transition : transitions) {
				if (transition.getInChar() == inChar) {
					if (correspondingStates.add(transition.getTo())) {
						result = Change.SOME;
					}
//					result.add(transition.getTo());
					if (correspondingTransitions.add(transition)) {
						result = Change.SOME;
					}
					anyTransition = true;
				}
			}
			if (!anyTransition) {
				correspondingStates.add(error);
				result = Change.ERROR;
//				result.add(error);
			}
			return result;
		}
		
		public 
//		Set<State>
		Change
		transitionFunction(Set<State> states, State error, Transition underlyingTransition) {
//			Set<State> result = new HashSet<State>();
			Change result = Change.NONE;
			for (State state : states) {
				Change res = transitionFunction(state, error, underlyingTransition);
				switch (res) {
				case SOME:
					if (result != Change.ERROR) {
						result = res;
					}
					break;
				case ERROR:
					result = res;
				}
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
