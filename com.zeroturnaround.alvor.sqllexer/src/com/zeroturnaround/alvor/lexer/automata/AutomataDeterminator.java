package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import ee.stacc.productivity.edsl.lexer.alphabet.SimpleCharacter;

public class AutomataDeterminator {
	
	public interface IStateSetPredicate {
		boolean accept(Set<State> states);
	}
	
	public static final IStateSetPredicate NONE = new IStateSetPredicate() {
		@Override
		public boolean accept(Set<State> states) {
			return false;
		}
	};
	
	/**
	 * @param initial an \epsilon-free automaton
	 * @return an initial state of an equivalent deterministic automaton (with SimpleCharacters as inputs)
	 */
	public static State determinate(State initial) {
		return new AutomataDeterminator(NONE, null).doDeterminate(initial);
	}

	public static State determinate(State initial, IStateSetPredicate predicate, Set<State> result) {
		return new AutomataDeterminator(predicate, result).doDeterminate(initial);
	}
	
	private final Map<State, Set<State>> singletons = new HashMap<State, Set<State>>();
	private final Map<Set<State>, Map<Integer, Set<State>>> newTransitions = new HashMap<Set<State>, Map<Integer,Set<State>>>();
	// Sets that have been offered to the queue
	private final Map<Set<State>, State> visited = new HashMap<Set<State>, State>();
	private final Queue<Set<State>> queue = new LinkedList<Set<State>>();
	
	private final IStateSetPredicate predicate;
	private final Set<State> selectedByPredicate;
	
	public AutomataDeterminator(IStateSetPredicate predicate,
			Set<State> selectedByPredicate) {
		this.predicate = predicate;
		this.selectedByPredicate = selectedByPredicate;
	}

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
			
			Map<Integer, Set<State>> newStateTransitions = new HashMap<Integer, Set<State>>();
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
			Map<Integer, Set<State>> transitionMap = newTransitions.get(oldStates);
			for (Entry<Integer, Set<State>> trEntry : transitionMap.entrySet()) {
				Integer integer = trEntry.getKey();
				Set<State> targetOldStates = trEntry.getValue();
				State targetNewState = visited.get(targetOldStates);
				
				Transition.create(
						newState, 
						targetNewState, 
						SimpleCharacter.create(integer));
			}
		}
	}

	private void joinMaps(Map<Integer, Set<State>> newStateTransitions,
			Map<Integer, Set<State>> transitions) {
		for (Entry<Integer, Set<State>> entry : transitions.entrySet()) {
			Integer Integer = entry.getKey();
			Set<State> smallTo = entry.getValue();
			Set<State> bigTo = AutomataTransduction.getSet(newStateTransitions, Integer);
			bigTo.addAll(smallTo);
		}
	}

	private Map<Integer, Set<State>> getTransitionMap(State oldState) {
		Set<State> singleton = singleton(oldState);
		Map<Integer, Set<State>> map = newTransitions.get(singleton);
		if (map == null) {
			map = new HashMap<Integer, Set<State>>();
			newTransitions.put(singleton, map);
			for (Transition transition : oldState.getOutgoingTransitions()) {
				encorporateTransition(map, transition);
			}
			enqueueTargets(map);
		}
		return map;
	}

	private void enqueueTargets(Map<Integer, Set<State>> map) {
		for (Set<State> newState : map.values()) {
			enqueue(newState);
		}
	}

	private void encorporateTransition(Map<Integer, Set<State>> map,
			Transition transition) {
		if (transition.isEmpty()) {
			throw new IllegalArgumentException("Only nonempty transitions suported");
		}
		Set<State> stateSet = AutomataTransduction.getSet(map, transition.getInChar().getCode());
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
		State newStateObject = new State(newState.toString(), accepting(newState));
		visited.put(newState, newStateObject);
		if (predicate.accept(newState)) {
			selectedByPredicate.add(newStateObject);
		}
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

	/**
	 * This operation changes the passed automaton
	 */
	public static void determinateWithPriorities(State initial) {
		Set<State> states = new HashSet<State>();
		EmptyTransitionEliminator.INSTANCE.dfs(initial, states);
		Set<Integer> usedIns = new HashSet<Integer>();
		for (State state : states) {
			usedIns.clear();
			Iterator<Transition> iterator = state.getOutgoingTransitionsNonConcurrent().iterator();
			while (iterator.hasNext()) {
				Transition transition = iterator.next();
				int inChar = transition.getInChar().getCode();
				if (usedIns.contains(inChar)) {
					transition.remove();
				} else {
					usedIns.add(inChar);
				}
			}
		}
	}
	
}
