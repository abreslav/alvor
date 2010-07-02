package ee.stacc.productivity.edsl.lexer.automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractOutputItem;

/**
 * Represents a transition in an automaton of transducer
 * 
 * @author abreslav
 *
 */
public class Transition {

	/**
	 * A factory method to create new transducer transitions. Must be used instead of the constructor
	 * @param from start state
	 * @param to end state
	 * @param inChar input character
	 * @param output output actions
	 * @return a new transition
	 */
	public static Transition create(State from, State to, IAbstractInputItem inChar,
			List<? extends IAbstractOutputItem> output) {
		return new Transition(from, to, inChar, output);
	}
	
	/**
	 * A factory method to create new automata transitions (no output actions). Must be used instead of the constructor
	 * @param from start state
	 * @param to end state
	 * @param inChar input character
	 * @return a new transition
	 */
	public static Transition create(State from, State to, IAbstractInputItem inChar) {
		return new Transition(from, to, inChar);
	}
	
	private final State from;
	private final State to;
	private final IAbstractInputItem inChar; 
	private final boolean empty; 
	private final List<IAbstractOutputItem> output; 
	
	/*
	 * This automatically puts the created transition into 
	 * 		from.getOutgoingTransitions() 
	 * 		and 
	 * 		to.getIncomingTransitions() 
	 */
	private Transition(State from, State to, IAbstractInputItem inChar,
			List<? extends IAbstractOutputItem> output) {
		if (output == null) {
			throw new IllegalArgumentException();
		}
		this.from = from;
		this.to = to;
		this.empty = inChar == null;
		this.inChar = empty ? null : inChar;
		this.output = Collections.unmodifiableList(new ArrayList<IAbstractOutputItem>(output));
		from.addOutgoing(this);
		to.addIncoming(this);
	}

	private Transition(State from, State to, IAbstractInputItem inChar) {
		this(from, to, inChar, Collections.<IAbstractOutputItem>emptyList());
	}
	
	/**
	 * Removes this transition from the automaton. 
	 * Automatically deletes it from all collections in start and end states
	 */
	public void remove() {
		from.removeOutgoing(this);
		to.removeIncoming(this);
	}
	
	/**
	 * @return true iff this transition has no input character
	 */
	public boolean isEmpty() {
		return empty;
	}
	
	/**
	 * @return start state
	 */
	public State getFrom() {
		return from;
	}

	/**
	 * @return end state
	 */
	public State getTo() {
		return to;
	}

	/**
	 * @return input character
	 */
	public IAbstractInputItem getInChar() {
		return inChar;
	}
	
	/**
	 * @return output actions
	 */
	public List<IAbstractOutputItem> getOutput() {
		return output;
	}

	@Override
	public String toString() {
		return from + " -" + inChar + "/" + output + "-> " + to;
	}
}