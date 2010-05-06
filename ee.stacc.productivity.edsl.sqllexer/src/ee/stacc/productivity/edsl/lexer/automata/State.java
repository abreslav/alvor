/**
 * 
 */
package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashSet;

public class State {
	private final String name;
	private final boolean accepting;
	private final Collection<Transition> outgoingTransitions = new LinkedHashSet<Transition>();
	private final Collection<Transition> incomingTransitions = new LinkedHashSet<Transition>();
	private final Iterable<Transition> outgoingTransitionsRO = Collections.unmodifiableCollection(outgoingTransitions);
	private final Iterable<Transition> incomingTransitionsRO = Collections.unmodifiableCollection(incomingTransitions);

	public State(String name, boolean accepting) {
		this.name = name;
		this.accepting = accepting;
	}

	public String getName() {
		return name;
	}

	/*package*/ void addIncoming(Transition transition) {
		incomingTransitions.add(transition);
	}
	
	/*package*/ void addOutgoing(Transition transition) {
		outgoingTransitions.add(transition);
	}
	
	/*package*/ void removeIncoming(Transition transition) {
		incomingTransitions.remove(transition);
	}
	
	/*package*/ void removeOutgoing(Transition transition) {
		outgoingTransitions.remove(transition);
	}
	
	/**
	 * This collection is filled in automatically when transitions are created
	 * The iterator() is vulnerable to {@link ConcurrentModificationException}, see {@link #getOutgoingTransitionsNonConcurrent()}
	 * To remove a transition, call its own method
	 */
	public Iterable<Transition> getOutgoingTransitions() {
		return outgoingTransitionsRO;
	}
	
	/**
	 * This collection is filled in automatically when transitions are created
	 * The iterator() is vulnerable to {@link ConcurrentModificationException}
	 * To remove a transition, call its own method
	 */
	public Iterable<Transition> getIncomingTransitions() {
		return incomingTransitionsRO;
	}
	
	public boolean hasOutgoingTransitions() {
		return !outgoingTransitions.isEmpty();
	}
	
	/**
	 * Returns a copy of the collection, safe to delete transitions on the go
	 * To remove a transition, call its own method
	 */
	public Iterable<Transition> getOutgoingTransitionsNonConcurrent() {
		return new LinkedHashSet<Transition>(outgoingTransitions);
	}
	
	public boolean isAccepting() {
		return accepting;
	}

	@Override
	public String toString() {
		return name;
	}
}