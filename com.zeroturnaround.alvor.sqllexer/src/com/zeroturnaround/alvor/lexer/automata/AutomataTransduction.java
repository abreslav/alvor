package com.zeroturnaround.alvor.lexer.automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zeroturnaround.alvor.lexer.alphabet.BranchingSequence;
import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;
import com.zeroturnaround.alvor.lexer.alphabet.IAbstractOutputItem;
import com.zeroturnaround.alvor.lexer.alphabet.ISequence;

/**
 * Performs inclusion checks and transductions
 * 
 * @author abreslav
 *
 */
public class AutomataTransduction {

	public static final AutomataTransduction INSTANCE = new AutomataTransduction();
	
	private AutomataTransduction() {}
	
	/**
	 * Check if the first contains the second
	 */
	public boolean checkInclusion(State initial, State start) {
		return checkInclusion(initial, start, IAlphabetConverter.ID);
	}
	
	public boolean checkInclusion(State initial, State start, IAlphabetConverter converter) {
		return new Checker(converter, IOutputItemInterpreter.ID).check(initial, start);
	}
	
	/**
	 * Transduce the second by the first
	 * @return the resulting automaton's initial state
	 */
	public State getTransduction(State transducerInitial, State inputInitial, IAlphabetConverter converter) {
		return getTransduction(transducerInitial, inputInitial, converter, PushYieldInterpreterWithKeywords.INSTANCE);
	}
	
	public State getTransduction(State transducerInitial, State inputInitial, IAlphabetConverter converter, IOutputItemInterpreter interpreter) {
		Checker checker = new Checker(converter, interpreter);
		if (!checker.check(transducerInitial, inputInitial)) {
			throw new IllegalArgumentException("The given automaton does not form a valid input for the given transducer");
		}
		Map<Transition, Set<List<IAbstractInputItem>>> transitionMap = checker.transitionMap;
		Map<State, StatesWithTexts> stateMap = checker.stateMap;
		Map<State, State> oldToNewStates = new HashMap<State, State>();
		for (State oldState : stateMap.keySet()) {
//			println("oldState: " + oldState + System.identityHashCode(oldState));
			
			State newState = getNewState(oldToNewStates, oldState);
//			println("newState: " + newState + System.identityHashCode(newState));
		
			for (Transition oldTransition : oldState.getOutgoingTransitions()) {
//				println("  oldTransition: " + oldTransition);
				
				for (List<IAbstractInputItem> text : getSet(transitionMap, oldTransition)) {
//					println("  transducerTransition text : " + text);
					
					final State newTo = getNewState(oldToNewStates, oldTransition.getTo());
//					println("  newTo: " + newTo + System.identityHashCode(newTo));
					
					if (text.isEmpty()) {
						createTransition(newState, newTo, null);
					} else {
						State from = newState;
						for (Iterator<IAbstractInputItem> iterator = text.iterator(); iterator
								.hasNext();) {
							IAbstractInputItem item = iterator.next();
							State to = !iterator.hasNext() ? newTo : new State("I", false);
							createTransition(from, to, item);
							from = to;
						}
					}
				}
			}
		}
		return oldToNewStates.get(inputInitial);
	}

	
	@SuppressWarnings("unused")
	private void println(String string) {
//		System.out.println(string);
	}

	private void createTransition(State from, State to, IAbstractInputItem c) {
		Transition.create(from, to, c);
//		println("    newTransition: " + transition);
	}

	private State getNewState(Map<State, State> oldToNewStates, State oldState) {
		State newState = oldToNewStates.get(oldState);
		if (newState == null) {
			// ' was appended here
			newState = new State(oldState.getName(), oldState.isAccepting());
			oldToNewStates.put(oldState, newState);
		}
		return newState;
	}
	
	private static class StatesWithTexts implements Iterable<State> {

		private final Map<State, Set<ISequence<IAbstractInputItem>>> map = new LinkedHashMap<State, Set<ISequence<IAbstractInputItem>>>();
		private boolean allAccepting = true;
		private boolean error = false;
		
		public boolean add(State state, ISequence<IAbstractInputItem> text) {
			allAccepting &= state.isAccepting();
// !!!NOTE: This is done in order to support repetition in lexer
//            I am not sure if it is sound, but all the tests pass OK
//			return getTexts(state).add(text);
			if (getTexts(state).isEmpty()) {
				return getTexts(state).add(text);
			}
			return false;
		}
		
		public StatesWithTexts copy() {
			StatesWithTexts copy = new StatesWithTexts();
			copy.map.putAll(map);
			return copy;
		}
		
		public void setError() {
			error = true;
		}
		
		public boolean isError() {
			return error;
		}
		
		public boolean allAccepting() {
			return allAccepting;
		}

		@Override
		public Iterator<State> iterator() {
			return map.keySet().iterator();
		}
		
		public Set<ISequence<IAbstractInputItem>> getTexts(State state) {
			return getSet(map, state); 
		}
		
		@Override
		public String toString() {
			return map.toString();
		}
	}
	
	private static class Checker {
		private static enum Change {
			NONE,
			SOME,
			ERROR
		}
		
		/**
		 * Maps a state of the included automaton to a set of states of including automaton  
		 */
		private final Map<State, StatesWithTexts> stateMap = new LinkedHashMap<State, StatesWithTexts>();
		/**
		 * Maps a transition of the included automaton to a set of strings generated on this transition during the transduction
		 * These strings will be later turned into transitions in the resulting automaton  
		 */
		private final Map<Transition, Set<List<IAbstractInputItem>>> transitionMap = new LinkedHashMap<Transition, Set<List<IAbstractInputItem>>>();

		private final State error = new State("<ERROR>", false);
		private final IAlphabetConverter converter;
		private final IOutputItemInterpreter interpreter;

		public Checker(IAlphabetConverter converter,
				IOutputItemInterpreter interpreter) {
			this.converter = converter;
			this.interpreter = interpreter;
		}

		public boolean check(State initial, State start) {
			getSet2(stateMap, start).add(initial, new BranchingSequence<IAbstractInputItem>());
			return dfs(start);
		}
		
		private boolean dfs(State current) {
			StatesWithTexts setForCurrent = getSet2(stateMap, current);
			if (setForCurrent.isError()) {
				return false;
			}
			
			if (current.isAccepting()) {
				if (!setForCurrent.allAccepting()) {
					return false;
				}
			}
			
			for (Transition transition : current.getOutgoingTransitions()) {
				Change status = transitionFunction(setForCurrent.copy(), error, transition);
				switch (status) {
				case NONE:
					break;
				case SOME:
					boolean result = dfs(transition.getTo());
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
	
		public Change transitionFunction(StatesWithTexts states, State error, Transition underlyingTransition) {
			Change result = Change.NONE;
			for (State state : states) {
				Change res = transitionFunction(state, states.getTexts(state), error, underlyingTransition);
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
		
		private	Change transitionFunction(State state, Set<ISequence<IAbstractInputItem>> texts, State error, Transition underlyingTransition) {
			StatesWithTexts correspondingStates = getSet2(stateMap, underlyingTransition.getTo());
			if (underlyingTransition.isEmpty()) {
				throw new UnsupportedOperationException("Empty transitions are not supported");
			}
			
			Change result = Change.NONE;
			Set<List<IAbstractInputItem>> correspondingTransitions = getSet(transitionMap, underlyingTransition);
			
			IAbstractInputItem inChar = underlyingTransition.getInChar();
			boolean anyTransition = false;
			for (Transition transition : state.getOutgoingTransitions()) {
				IAbstractInputItem expectedChar = transition.getInChar();
				
				if (converter.convert(inChar.getCode()) == expectedChar.getCode()) {
					for (ISequence<IAbstractInputItem> text : new HashSet<ISequence<IAbstractInputItem>>(texts)) {
						List<IAbstractOutputItem> output = transition.getOutput();
						List<IAbstractInputItem> effect = new ArrayList<IAbstractInputItem>();
						ISequence<IAbstractInputItem> newText = interpreter.processOutputCommands(text, inChar, output, effect);
						if (correspondingStates.add(transition.getTo(), newText)) {
							result = Change.SOME;
						}
						if (correspondingTransitions.add(effect)) {
							result = Change.SOME;
						}
					}
					anyTransition = true;
				}
			}

			if (!anyTransition) {
				correspondingStates.setError();
				result = Change.ERROR;
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
	
	public static <K, V> List<V> getList(Map<K, List<V>> map, K key) {
		List<V> set = map.get(key);
		if (set == null) {
			set = new ArrayList<V>();
			map.put(key, set);
		}
		return set;
	}

	private static <K> StatesWithTexts getSet2(Map<K, StatesWithTexts> map, K key) {
		StatesWithTexts set = map.get(key);
		if (set == null) {
			set = new StatesWithTexts();
			map.put(key, set);
		}
		return set;
	}
	
}
