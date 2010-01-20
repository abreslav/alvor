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
		return new Checker(initial, start).check();
	}
	
	private static class Checker {

		private final Map<State, Set<State>> reachable = new HashMap<State, Set<State>>();
		private final State error = new State("<ERROR>", false);
		private final State initial;
		private final State start;
		
		public Checker(State initial, State start) {
			this.initial = initial;
			this.start = start;
		}

		public boolean check() {
			return dfs(start, Collections.singleton(initial));
		}
		
		private boolean dfs(State current, Set<State> incoming) {
			Set<State> setForCurrent = getSet(reachable, current);
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
				Set<State> newSet = TransitionFunction.INSTANCE.apply(
						setForCurrent, 
						error, 
						transition.getSet());
				boolean result = dfs(
					transition.getTo(), 
					newSet);
				if (!result) {
					return false;
				}
			}
			return true;
		}
		
//		private boolean isStarted(Map<State, Set<State>> reachable, State state) {
//			return reachable.containsKey(state);
//		}
//		
		private Set<State> getSet(Map<State, Set<State>> reachable, State state) {
			Set<State> set = reachable.get(state);
			if (set == null) {
				set = new HashSet<State>();
				reachable.put(state, set);
			}
			return set;
		}
		
	}
	
}
