package com.zeroturnaround.alvor.sqlparser.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;
import com.zeroturnaround.alvor.lexer.automata.State;
import com.zeroturnaround.alvor.lexer.automata.Transition;
import com.zeroturnaround.alvor.sqlparser.framework.priorityqueue.BinaryHeap;
import com.zeroturnaround.alvor.sqlparser.framework.priorityqueue.IPriorityQueue;
import com.zeroturnaround.alvor.sqlparser.framework.priorityqueue.IPriorityQueueItem;

/**
 * Finds the shortest counter example by Dijkstra over predicate states 
 * Currently, this is not needed: edge length is always one, but if we 
 * looked for another metric (e.g. shortest in terms of characters, not tokens),
 * this would make sense.  
 * 
 * @author abreslav
 *
 * @param <S> the predicate state type
 */
public class DijkstraAbstractInterpreter<S> implements IAbstractInterpreter<S> {

	private class PredicateState implements IPriorityQueueItem<PredicateState> {
		private final S data;
		private PredicateState cameHereFrom;
		private Transition cameHereBy;
		private final State inputState;
		private int pathLength;
		private int heapIndex;
		
		public PredicateState(S data, PredicateState cameHereFrom,
				Transition cameHereBy) {
			this(data, cameHereBy.getTo(), cameHereFrom, cameHereBy);
		}
		
		public PredicateState(S data, State inputState,
					PredicateState cameHereFrom, Transition cameHereBy) {
			this.data = data;
			this.cameHereFrom = cameHereFrom;
			this.cameHereBy = cameHereBy;
			if (cameHereFrom != null) {
				this.pathLength = cameHereFrom.pathLength + transitionLength.length(cameHereBy);
				assert inputState == cameHereBy.getTo();
			}
			this.inputState = inputState; 
		}

		public S getData() {
			return data;
		}
		
		public boolean tryAnotherPredecessor(PredicateState cameFrom, Transition cameBy) {
			int newLength = cameFrom.pathLength + transitionLength.length(cameBy);
			if (newLength > pathLength) {
				return false;
			}
			pathLength = newLength;
			cameHereBy = cameBy;
			cameHereFrom = cameFrom;
			return true;
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
			final int prime = 31;
			int result = 1;
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
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

		@Override
		public int getIndex() {
			return heapIndex;
		}

		@Override
		public void setIndex(int index) {
			this.heapIndex = index;
		}

		@Override
		public int compareTo(IPriorityQueueItem<PredicateState> o) {
			// This works only for nonnegative numbers
			return this.pathLength - o.getContents().pathLength;
		}
		
		@Override
		public PredicateState getContents() {
			return this;
		}

	}
	
	private final Map<State, Map<S, PredicateState>> stateMap = new HashMap<State, Map<S, PredicateState>>(); 
	private final IAbstractablePredicate<S, IAbstractInputItem> predicate;
	private final ITransitionLength transitionLength;
	
	public DijkstraAbstractInterpreter(IAbstractablePredicate<S, IAbstractInputItem> predicate, ITransitionLength transitionLength) {
		this.predicate = predicate;
		this.transitionLength = transitionLength;
	}	
	
	public IError interpret(State inputInitalState, List<IAbstractInputItem> counterExample) {
		PredicateState initialPredicateState = new PredicateState(predicate.getInitialState(), inputInitalState, null, null);
		getPredicateStates(inputInitalState).put(initialPredicateState.data, initialPredicateState);
		
		IPriorityQueue<PredicateState> queue = new BinaryHeap<PredicateState>();

		queue.enqueue(initialPredicateState);		
		
		while (!queue.isEmpty()) {
			PredicateState predicateStateFrom = queue.dequeue();
			
			for (Transition transition : predicateStateFrom.inputState.getOutgoingTransitions()) {
				IAbstractInputItem inputChar = transition.getInChar();
				State inputTo = transition.getTo();
				Map<S, PredicateState> predicateStatesTo = getPredicateStates(inputTo);				
				
				S newPSTo = predicate.transition(predicateStateFrom.getData(), inputChar);
				
				PredicateState predicateStateTo = predicateStatesTo.get(newPSTo);
				if (predicateStateTo == null) {
					PredicateState newPredicateState = new PredicateState(newPSTo, predicateStateFrom, transition);
					IError error = predicate.getError(newPSTo);
					if (error != IError.NO_ERROR) {
						if (counterExample != null) {
							generateCounterExample(counterExample, newPredicateState, inputTo);
						}
						return error;
					}
					predicateStatesTo.put(newPSTo, newPredicateState);
					queue.enqueue(newPredicateState);
				} else {
					if (predicateStateTo.tryAnotherPredecessor(predicateStateFrom, transition)) {
						queue.handleDecreasedKey(predicateStateTo);
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

	private Map<S, PredicateState> getPredicateStates(State inputState) {
		Map<S, PredicateState> set = stateMap.get(inputState);
		if (set == null) {
			set = new HashMap<S, PredicateState>();
			stateMap.put(inputState, set);
		}
		return set;
	}
}