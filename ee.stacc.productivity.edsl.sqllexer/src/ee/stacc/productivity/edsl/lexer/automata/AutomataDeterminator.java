package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

public class AutomataDeterminator {
	private final Map<State, Set<State>> singletons = new HashMap<State, Set<State>>();
	private final Map<Set<State>, Map<Character, Set<State>>> newTransitions = new HashMap<Set<State>, Map<Character,Set<State>>>();
	// Sets that have been offered to the queue
	private final Map<Set<State>, State> visited = new HashMap<Set<State>, State>();
	private final Queue<Set<State>> queue = new LinkedList<Set<State>>();
	
	/**
	 * @param initial an \epsilon-free automaton
	 * @return an initial state of an equivalent deterministic automaton
	 */
	public static State determinate(State initial) {
		return new AutomataDeterminator().doDeterminate(initial);
	}
	
	private AutomataDeterminator() {}
	
	private State doDeterminate(State initial) {
		
		Set<State> newInitial = Collections.singleton(initial);

		findAllTransitions(newInitial);
		
		createAllTransitions();
		
		return visited.get(newInitial);
	}

	private void findAllTransitions(Set<State> newInitial) {
		enqueue(newInitial);
		while (!queue.isEmpty()) {
			Set<State> newState = dequeue();
			
			if (newState.size() == 1) {
				getTransitionMap(newState.iterator().next());
				continue;
			}
			
			Map<Character, Set<State>> newStateTransitions = new HashMap<Character, Set<State>>();
			for (State oldState : newState) {
				joinMaps(newStateTransitions, getTransitionMap(oldState));
			}
			newTransitions.put(newState, newStateTransitions);
			enqueueTargets(newStateTransitions);
		}
	}

	private void createAllTransitions() {
		for (Entry<Set<State>, State> entry : visited.entrySet()) {
			Set<State> oldStates = entry.getKey();
			State newState = entry.getValue();
			Map<Character, Set<State>> transitionMap = newTransitions.get(oldStates);
			for (Entry<Character, Set<State>> trEntry : transitionMap.entrySet()) {
				Character character = trEntry.getKey();
				Set<State> targetOldStates = trEntry.getValue();
				State targetNewState = visited.get(targetOldStates);
				
				Transition transition = new Transition(
						newState, 
						targetNewState, 
						character);
				newState.getOutgoingTransitions().add(transition);
			}
		}
	}

	private void joinMaps(Map<Character, Set<State>> newStateTransitions,
			Map<Character, Set<State>> transitions) {
		for (Entry<Character, Set<State>> entry : transitions.entrySet()) {
			Character character = entry.getKey();
			Set<State> smallTo = entry.getValue();
			Set<State> bigTo = AutomataInclusion.getSet(newStateTransitions, character);
			bigTo.addAll(smallTo);
		}
	}

	private Map<Character, Set<State>> getTransitionMap(State oldState) {
		Set<State> singleton = singleton(oldState);
		Map<Character, Set<State>> map = newTransitions.get(singleton);
		if (map == null) {
			map = new HashMap<Character, Set<State>>();
			newTransitions.put(singleton, map);
			for (Transition transition : oldState.getOutgoingTransitions()) {
				encorporateTransition(map, transition);
			}
			enqueueTargets(map);
		}
		return map;
	}

	private void enqueueTargets(Map<Character, Set<State>> map) {
		for (Set<State> newState : map.values()) {
			enqueue(newState);
		}
	}

	private void encorporateTransition(Map<Character, Set<State>> map,
			Transition transition) {
		if (transition.isEmpty()) {
			throw new IllegalArgumentException("Only nonempty transitions suported");
		}
		Set<State> stateSet = AutomataInclusion.getSet(map, transition.getInChar());
		stateSet.add(transition.getTo());
	}

	private Set<State> singleton(State state) {
		Set<State> singleton = singletons.get(state);
		if (singleton == null) {
			singleton = Collections.singleton(state);
			singletons.put(state, singleton);
		}
		return singleton;
	}
	
	private Set<State> dequeue() {
		return queue.poll();
	}

	private void enqueue(Set<State> newState) {
		if (visited.containsKey(newState)) {
			return;
		}
		visited.put(newState, new State(newState.toString(), accepting(newState)));
		queue.offer(newState);
	}

	private boolean accepting(Set<State> newState) {
		for (State state : newState) {
			if (state.isAccepting()) {
				return true;
			}
		}
		return false;
	}
	
}
