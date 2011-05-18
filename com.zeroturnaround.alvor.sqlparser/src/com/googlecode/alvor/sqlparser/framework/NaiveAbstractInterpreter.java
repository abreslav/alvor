package com.googlecode.alvor.sqlparser.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.automata.State;
import com.googlecode.alvor.lexer.automata.Transition;

/**
 * Finds some counter example by BFS over the input states
 * 
 * @author abreslav
 *
 * @param <S> the predicate state type
 */
public class NaiveAbstractInterpreter<S> implements IAbstractInterpreter<S> {

	private class PredicateState {
		private final S data;
		private PredicateState cameHereFrom;
		private Transition cameHereBy;
		
		public PredicateState(S data, PredicateState cameHereFrom,
				Transition cameHereBy) {
			this.data = data;
			this.cameHereFrom = cameHereFrom;
			this.cameHereBy = cameHereBy;
		}

		public S getData() {
			return data;
		}
		
		public void getPath(List<IAbstractInputItem> inputChars, Set<State> inputStatesVisited) {
			if (cameHereFrom != null) {
				cameHereFrom.getPath(inputChars, inputStatesVisited);
				inputStatesVisited.add(cameHereBy.getFrom());
				inputChars.add(cameHereBy.getInChar());
			}
		}

		@Override
		public int hashCode() {
			return 31 + ((data == null) ? 0 : data.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("unchecked")
			PredicateState other = (PredicateState) obj;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return data.toString();
		}

	}
	
	private final Map<State, Set<PredicateState>> stateMap = new HashMap<State, Set<PredicateState>>(); 
	private final IAbstractablePredicate<S, IAbstractInputItem> predicate;
	
	public NaiveAbstractInterpreter(IAbstractablePredicate<S, IAbstractInputItem> predicate) {
		this.predicate = predicate;
	}	
	
	public IError interpret(State inputInitalState, List<IAbstractInputItem> counterExample) {
		getPredicateStates(inputInitalState).add(new PredicateState(predicate.getInitialState(), null, null));
		Queue<State> queue = new LinkedList<State>();
		Set<State> inQueue = new HashSet<State>();

		queue.add(inputInitalState);		
		inQueue.add(inputInitalState);
		
		while (!queue.isEmpty()) {
			State inputFrom = queue.poll();
			inQueue.remove(inputFrom);
			
			Set<PredicateState> predicateStatesFrom = getPredicateStates(inputFrom);

//	The input automaton is supposed to be finished by an EOF and have only one accepting state 
//			if (inputFrom.isAccepting() && !admitsEOF(predicateStatesFrom)) {
//				return false;
//			}			
			
			for (Transition transition : inputFrom.getOutgoingTransitions()) {
				IAbstractInputItem inputChar = transition.getInChar();
				State inputTo = transition.getTo();
				Set<PredicateState> predicateStatesTo = getPredicateStates(inputTo);				
				
				for (PredicateState predicateStateFrom : predicateStatesFrom) {
					S newPSTo = predicate.transition(predicateStateFrom.getData(), inputChar);
					PredicateState newPredicateState = new PredicateState(newPSTo, predicateStateFrom, transition);
					IError error = predicate.getError(newPSTo);
					if (error != IError.NO_ERROR) {
						if (counterExample != null) {
							generateCounterExample(counterExample, newPredicateState, inputTo);
						}
						return error;
					}
					if (predicateStatesTo.add(newPredicateState)) {
						if (inQueue.add(inputTo)) {
							queue.add(inputTo);
						}
					}
				}
			}
		}
		return IError.NO_ERROR;
	}

	private void generateCounterExample(
			List<IAbstractInputItem> counterExample,
			PredicateState errorPredicateState, State errorInputState) {
		Set<State> visited = new HashSet<State>();
		errorPredicateState.getPath(counterExample, visited);
		Queue<State> queue = new LinkedList<State>();
		queue.offer(errorInputState);
		visited.add(errorInputState);
		
		Map<State, Transition> cameBy = new HashMap<State, Transition>();
		
		
		State acceptingState = null;
		while (!queue.isEmpty()) {
			State state = queue.poll();
			if (state.isAccepting()) {
				acceptingState = state;
				break;
			}
			
			for (Transition transition : state.getOutgoingTransitions()) {
				State inputTo = transition.getTo();
				if (visited.add(inputTo)) {
					queue.add(inputTo);
					cameBy.put(inputTo, transition);
				}
			}
		}
		assert acceptingState != null;
		assert acceptingState.isAccepting();
		
		List<IAbstractInputItem> suffix = new ArrayList<IAbstractInputItem>();
		State state = acceptingState;
		while (state != errorInputState) {
			Transition transition = cameBy.get(state);
			assert transition != null;
			state = transition.getFrom();
			
			suffix.add(transition.getInChar());
		}
		Collections.reverse(suffix);
		counterExample.addAll(suffix);
	}
	
//	private boolean admitsEOF(Set<S> predicateStatesFrom) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	private Set<PredicateState> getPredicateStates(State inputState) {
		Set<PredicateState> set = stateMap.get(inputState);
		if (set == null) {
			set = new LinkedHashSet<PredicateState>();
			stateMap.put(inputState, set);
		}
		return set;
	}
}