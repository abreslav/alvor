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
	private final Iterable<Transition> outgoingTransitionsRO = Collections.unmodifiableCollection(outgoingTransitions);

	private Collection<Transition> incomingTransitions;
	private Iterable<Transition> incomingTransitionsRO;

	public State(String name, boolean accepting) {
		this.name = name;
		this.accepting = accepting;
	}

	public String getName() {
		return name;
	}
	
	/*package*/ void initializeIncomingTransitions() {
		if (incomingTransitions != null) {
			return;
		}
		incomingTransitions = new LinkedHashSet<Transition>();
		incomingTransitionsRO = Collections.unmodifiableCollection(incomingTransitions);
	}

	/*package*/ void addIncoming(Transition transition) {
		if (incomingTransitions != null) {
			incomingTransitions.add(transition);
		}
	}
	
	/*package*/ void removeIncoming(Transition transition) {
		if (incomingTransitions != null) {
			incomingTransitions.remove(transition);
		}
	}
	
	/*package*/ void addOutgoing(Transition transition) {
		outgoingTransitions.add(transition);
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
		if (incomingTransitions == null) {
			throw new IllegalStateException("Incoming transitions are not intialized");
		}
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