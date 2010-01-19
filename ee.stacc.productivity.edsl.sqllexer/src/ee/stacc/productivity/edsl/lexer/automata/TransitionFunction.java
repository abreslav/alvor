package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ee.stacc.productivity.edsl.lexer.CharacterSetFactory;
import ee.stacc.productivity.edsl.lexer.ICharacterSet;

public class TransitionFunction {

	public static final TransitionFunction INSTANCE = new TransitionFunction();
	
	private TransitionFunction() {}
	
	private Set<State> apply(State state, State error, ICharacterSet chars) {
		Set<State> result = new HashSet<State>();
		Collection<Transition> transitions = state.getOutgoingTransitions();
		ICharacterSet allTransitions = CharacterSetFactory.empty();
		for (Transition transition : transitions) {
			ICharacterSet set = transition.getSet();
			if (set.intersects(chars)) {
				result.add(transition.getTo());
				allTransitions = allTransitions.join(set);
			}
		}
		if (!allTransitions.containsSet(chars)) {
			result.add(error);
		}
		return result;
	}
	
	public Set<State> apply(Set<State> states, State error, ICharacterSet chars) {
		Set<State> result = new HashSet<State>();
		for (State state : states) {
			result.addAll(apply(state, error, chars));
		}	
		return result;
	}
	
}
