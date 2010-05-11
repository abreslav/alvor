package ee.stacc.productivity.edsl.completion;

import java.util.HashSet;
import java.util.Set;

import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.Transition;

public class ContextRecognizer {

	public interface ITraversalStrategy {
		
		ITraversalStrategy FORWARD = new ITraversalStrategy() {
			
			@Override
			public Iterable<Transition> getTransitions(State state) {
				return state.getOutgoingTransitions();
			}
			
			@Override
			public State getTarget(Transition transition) {
				return transition.getTo();
			}
		};
		
		ITraversalStrategy BACKWARD = new ITraversalStrategy() {
			
			@Override
			public Iterable<Transition> getTransitions(State state) {
				return state.getIncomingTransitions();
			}
			
			@Override
			public State getTarget(Transition transition) {
				return transition.getFrom();
			}
		};
		
		Iterable<Transition> getTransitions(State state);
		State getTarget(Transition transition);
	}
	
	public static final ContextRecognizer INSTANCE = new ContextRecognizer();
	
	private ContextRecognizer() {}
	
	public boolean isBetween(Transition transition, int tokenTypeLeft, int tokenTypeRight) { 
		return 
			tokenOnAllPaths(transition, tokenTypeRight, ITraversalStrategy.FORWARD)
			&& tokenOnAllPaths(transition, tokenTypeLeft, ITraversalStrategy.BACKWARD)
			;
	}

	private boolean tokenOnAllPaths(Transition transition, int tokenType,
			ITraversalStrategy strategy) {
		Token token = TokenLocator.getToken(transition);
		if (token.getCode() == tokenType) {
			return true;
		}
		return tokenOnAllPaths(strategy.getTarget(transition), tokenType, strategy, new HashSet<State>());
	}

	// Here we assume that if there is a back-edge (transition) in the graph, then
	// if we ignore this edge, we gain the same amount of information, as if we processed it
	private boolean tokenOnAllPaths(State state, int tokenType, ITraversalStrategy strategy, Set<State> started) {
		started.add(state);
		
		boolean anyTransitions = false;
		for (Transition next : strategy.getTransitions(state)) {
			anyTransitions = true;
			Token token = TokenLocator.getToken(next);
			if (token.getCode() != tokenType) {
				State to = strategy.getTarget(next);
				if (!started.contains(to)) {
					if (!tokenOnAllPaths(to, tokenType, strategy, started)) {
						return false;
					}
				}
			}
		}
		// If there are no transitions, we return false
		return anyTransitions;
	}
}
