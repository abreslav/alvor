/**
 * 
 */
package ee.stacc.productivity.edsl.lexer.automata;

import java.util.ArrayList;
import java.util.Collection;

public class State {
	private final String name;
	private final boolean accepting;
	private final Collection<Transition> incomingTransitions = new ArrayList<Transition>();
	private final Collection<Transition> outgoingTransitions = new ArrayList<Transition>();

	public State(String name, boolean accepting) {
		this.name = name;
		this.accepting = accepting;
	}

	public String getName() {
		return name;
	}

	public Collection<Transition> getIncomingTransitions() {
		return incomingTransitions;
	}

	public Collection<Transition> getOutgoingTransitions() {
		return outgoingTransitions;
	}
	
	public boolean isAccepting() {
		return accepting;
	}

	@Override
	public String toString() {
		return "[" + name + "]";
	}
}