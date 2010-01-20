package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ee.stacc.productivity.edsl.lexer.CharacterSetFactory;
import ee.stacc.productivity.edsl.lexer.ICharacterSet;

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
			throw new IllegalArgumentException("The given automata does not form a valid input for the given transducer");
		}
		Map<Transition, Set<Transition>> transitionMap = checker.transitionMap;
		Map<State, State> oldToNewStates = new HashMap<State, State>();
		Set<State> oldStates = checker.stateMap.keySet();
		for (State oldState : oldStates) {
			State newState = getNewState(oldToNewStates, oldState);
			Collection<Transition> oldTransitions = oldState.getOutgoingTransitions();
			for (Transition oldTransition : oldTransitions) {
				Set<Transition> transducerTransitions = getSet(transitionMap, oldTransition);
				Set<Character> chars = new HashSet<Character>();
				for (Transition transducerTransition : transducerTransitions) {
					String outStr = transducerTransition.getOutStr();
					if (outStr.length() > 0) {
						chars.add(outStr.charAt(0));
					}
				}
				newState.getOutgoingTransitions().add(
						new Transition(
								newState, 
								getNewState(oldToNewStates, oldTransition.getTo()), 
								CharacterSetFactory.set(chars)));
			}
		}
		return oldToNewStates.get(inputInitial);
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
			Collection<Transition> transitions = state.getOutgoingTransitions();
			Set<Transition> correspondingTransitions = getSet(transitionMap, underlyingTransition);
			ICharacterSet allTransitions = CharacterSetFactory.empty();
			ICharacterSet inSet = underlyingTransition.getInSet();
			for (Transition transition : transitions) {
				ICharacterSet set = transition.getInSet();
				if (set.intersects(inSet)) {
					result.add(transition.getTo());
					correspondingTransitions.add(transition);
					allTransitions = allTransitions.join(set);
				}
			}
			if (!allTransitions.containsSet(inSet)) {
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

	private static <K, V> Set<V> getSet(Map<K, Set<V>> map, K key) {
		Set<V> set = map.get(key);
		if (set == null) {
			set = new HashSet<V>();
			map.put(key, set);
		}
		return set;
	}
	
}
