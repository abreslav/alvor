/**
 * 
 */
package com.googlecode.alvor.lexer.automata;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashSet;

/**
 * Represents states of an automaton or transducer (an automaton is considered a special case 
 * of a transducer -- no output actions of transitions).
 * 
 * @author abreslav
 *
 */
public class State {
	private final String name;
	private final boolean accepting;
	private final Collection<Transition> outgoingTransitions = new LinkedHashSet<Transition>();
	private final Iterable<Transition> outgoingTransitionsRO = Collections.unmodifiableCollection(outgoingTransitions);

	private Collection<Transition> incomingTransitions;
	private Iterable<Transition> incomingTransitionsRO;

	/**
	 * @param name a symbolic name for the state, needed for debugging purposes only
	 * @param accepting whether this is an accepting state of the corresponding automaton
	 */
	public State(String name, boolean accepting) {
		this.name = name;
		this.accepting = accepting;
	}

	/**
	 * @return symbolic name of the state (needed for debugging only)
	 */
	public String getName() {
		return name;
	}
	
	/*
	 * This is called only by {@link IncomingTransitionsInitializer} 
	 */
	/*package*/ void initializeIncomingTransitions() {
		if (incomingTransitions != null) {
			return;
		}
		incomingTransitions = new LinkedHashSet<Transition>();
		incomingTransitionsRO = Collections.unmodifiableCollection(incomingTransitions);
	}

	/*
	 * This is called only by {@link Transition} 
	 */
	/*package*/ void addIncoming(Transition transition) {
		if (incomingTransitions != null) {
			incomingTransitions.add(transition);
		}
	}
	
	/*
	 * This is called only by {@link Transition} 
	 */
	/*package*/ void removeIncoming(Transition transition) {
		if (incomingTransitions != null) {
			incomingTransitions.remove(transition);
		}
	}
	
	/*
	 * This is called only by {@link Transition} 
	 */
	/*package*/ void addOutgoing(Transition transition) {
		outgoingTransitions.add(transition);
	}
	
	/*
	 * This is called only by {@link Transition} 
	 */
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
	 * 
	 * NOTE: this method will throw an exception, unless {@link IncomingTransitionsInitializer} was invoked 
	 * explicitly on the automaton containing this state (automata are represented by their initial state)
	 */
	public Iterable<Transition> getIncomingTransitions() {
		if (incomingTransitions == null) {
			throw new IllegalStateException("Incoming transitions are not intialized");
		}
		return incomingTransitionsRO;
	}
	
	/**
	 * @return true iff there are outgoing transitions in this state
	 */
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
	
	/**
	 * @return true if this is an accepting state
	 */
	public boolean isAccepting() {
		return accepting;
	}

	@Override
	public String toString() {
		return name;
	}
}
