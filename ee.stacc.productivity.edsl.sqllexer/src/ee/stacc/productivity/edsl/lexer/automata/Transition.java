/**
 * 
 */
package ee.stacc.productivity.edsl.lexer.automata;

import ee.stacc.productivity.edsl.lexer.ICharacterSet;

public class Transition {
	private final State from;
	private final State to;
	private final ICharacterSet set; 
	
	public Transition(State from, State to, ICharacterSet set) {
		this.from = from;
		this.to = to;
		this.set = set;
	}
	
	public State getFrom() {
		return from;
	}

	public State getTo() {
		return to;
	}

	public ICharacterSet getSet() {
		return set;
	}

	@Override
	public String toString() {
		return from + " -> " + to;
	}
}