package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.automata.AutomataParser.Automaton;


public class AutomataInclusionTest {

	@Test
	public void testInclusion() throws Exception {
		String automaton1;
		String automaton2;

		
		automaton1 = "A - !B:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);

		
		automaton1 = "!A - !B:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		automaton1 = "!A - !A:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
				"!A - B:q C:x;" +
				"B - B:k C:m;" +
				"C - !D:l";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);

		
		automaton1 = "!A - !A:x;";
		automaton2 = 
			"!A - !B:x;" +
			"!B - !B:x";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = "!A1 - !B1:y;";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = 
				"!A1 - !B1:x;" +
				"!B1 - !B1:y";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = 
			"!A1 - !B1:y;" +
			"!B1 - !B1:x";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"A1 - A2:a !A3:a;" +
			"!A2 - A1:b";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"!A1 - !A2:a !A3:a;" +
			"!A2 - !A1:b";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a S1:x;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"A1 - A2:a !A3:a;" +
			"!A2 - A1:b";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"A1 - A2:a !A3:a A1:x;" +
			"!A2 - A1:b";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
	}
	
	
	@Test
	public void testTransduction() throws Exception {
		String automatonStr;
		String transducerStr;
		String checkStr;
		Automaton automaton;
		Automaton transducer;
		Automaton check;
		State transduction;

		automatonStr = "S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		transducerStr = "S1 - !S2:a/x;" +
			"!S2 - S3:b/y;" +
			"S3 - S2:a/z";
		checkStr = "S1 - !S2:x;" +
			"!S2 - S3:y;" +
			"S3 - S2:z";

		automaton = AutomataParser.parse(automatonStr);
		transducer = AutomataParser.parse(transducerStr);
		check = AutomataParser.parse(checkStr);
		transduction = AutomataInclusion.INSTANCE.getTrasduction(transducer.getInitialState(), automaton.getInitialState());
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(check.getInitialState(), transduction));
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(transduction, check.getInitialState()));

		// 0*_+("")*0
		automatonStr = 
			"A1 - A1:0 A2:_;" +
			"A2 - A2:_ A3:\" !A5:0;" +
			"A3 - A4:\";" +
			"A4 - A3:\" !A5:0";
		// (0+/V | "a*"/S | _)*
		transducerStr = 
			"!T1 - !T1:_ !T2:0/V T3:\" ;" +
			"!T2 - !T2:0 !T1:_ T3:\" ;" +
			"T3 - T3:a !T1:\"/S ;";
		// V*S*V
		checkStr = 
			"C1 - C1:V C2:S !C3:V;" +
			"C2 - C2:S !C3:V;";
		
		automaton = AutomataParser.parse(automatonStr);
		transducer = AutomataParser.parse(transducerStr);
		check = AutomataParser.parse(checkStr);
		transduction = AutomataInclusion.INSTANCE.getTrasduction(transducer.getInitialState(), automaton.getInitialState());
		transduction = eliminateEmptySetTransitions(transduction);
		System.out.println(transduction.getOutgoingTransitions());
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(transduction, check.getInitialState()));
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(check.getInitialState(), transduction));
		
	}
	
	private static final class DisjointSets {

		private final Random random = new Random();
		private final int[] parents;
		
		public DisjointSets(int size) {
			this.parents = new int[size];
		}

		public void add(int x) {
			parents[x] = x;
		}
		
		public int find(int x) {
			if (parents[x] == x) {
				return x;
			} else {
				int representative = find(parents[x]);
				parents[x] = representative;
				return representative;
			}
		}
		
		public void union(int x, int y) {
			int xr = find(x);
			int yr = find(y);
			if (random.nextBoolean()) {
				parents[y] = xr;
			} else {
				parents[x] = yr;
			}
		}
		
	}
	
	private static final class DisjointSetsOf<E> {
		private final DisjointSets disjointSets;
		private final Map<E, Integer> map = new HashMap<E, Integer>();
		private final Object[] elements;
		private int index = 0;
		
		public DisjointSetsOf(int size) {
			this.disjointSets = new DisjointSets(size);
			this.elements = new Object[size];
		}
		
		public boolean add(E e) {
			if (map.containsKey(e)) {
				return false;
			}
			int ind = index;
			index++;
			map.put(e, ind);
			elements[ind] = e;
			disjointSets.add(ind);
			return true;
		}
		
		public E find(E e) {
			int ind = disjointSets.find(map.get(e));
			@SuppressWarnings("unchecked")
			E representative = (E) elements[ind];
			return representative;
		}
		
		public void union(E e1, E e2) {
			int ind1 = map.get(e1);
			int ind2 = map.get(e2);
			disjointSets.union(ind1, ind2);
		}
		
	}
	
	/**
	 * This \epsilon-closes the automaton
	 */
	private State eliminateEmptySetTransitions(State intial) {
		HashSet<State> states = new HashSet<State>();
		dfs(intial, states);
		DisjointSetsOf<State> sets = new DisjointSetsOf<State>(states.size());
		for (State state : states) {
			close(state, state, sets);
		}
		
		Set<State> accepting = new HashSet<State>();
		for (State state : states) {
			if (state.isAccepting()) {
				accepting.add(sets.find(state));
			}
		}
		
		Map<State, State> newStates = new HashMap<State, State>();
		for (State oldState : states) {
			
			
			State newState = getNewState(newStates, oldState, accepting, sets);
			
			for (Transition oldTransition : oldState.getOutgoingTransitions()) {
				if (!oldTransition.getInSet().isEmpty()) {
					newState.getOutgoingTransitions().add(
							new Transition(
									newState, 
									getNewState(
											newStates, 
											oldTransition.getTo(), 
											accepting, 
											sets),
									oldTransition.getInSet()
							));
				}
			}
		}
		
		return newStates.get(intial);
	}


	private State getNewState(Map<State, State> newStates,
			State oldState, Set<State> accepting, DisjointSetsOf<State> sets) {
		State representative = sets.find(oldState);
		State newState = newStates.get(representative);
		if (newState == null) {
			newState = new State(representative.getName() + "'", accepting.contains(representative));
			newStates.put(representative, newState);
		}
		return newState;
	}
	
	private void dfs(State start, Set<State> visited) {
		if (visited.contains(start)) {
			return;
		}
		
		visited.add(start);
		
		Collection<Transition> outgoingTransitions = start.getOutgoingTransitions();
		for (Transition transition : outgoingTransitions) {
			dfs(transition.getTo(), visited);
		}
	}


	private void close(State state, State rep, DisjointSetsOf<State> sets) {
		if (!sets.add(state)) {
			return;
		}
		sets.union(state, rep);
		Collection<Transition> outgoingTransitions = state.getOutgoingTransitions();
		for (Transition transition : outgoingTransitions) {
			if (transition.getInSet().isEmpty()) {
				close(transition.getTo(), rep, sets);
			}
		}
	}
	
}