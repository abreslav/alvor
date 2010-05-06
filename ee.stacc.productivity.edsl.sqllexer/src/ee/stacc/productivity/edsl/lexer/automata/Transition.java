package ee.stacc.productivity.edsl.lexer.automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractOutputItem;


public class Transition {

	public static Transition create(State from, State to, IAbstractInputItem inChar,
			List<? extends IAbstractOutputItem> output) {
		return new Transition(from, to, inChar, output);
	}
	
	public static Transition create(State from, State to, IAbstractInputItem inChar) {
		return new Transition(from, to, inChar);
	}
	
	private final State from;
	private final State to;
	private final IAbstractInputItem inChar; 
	private final boolean empty; 
	private final List<IAbstractOutputItem> output; 
	
	/**
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
	
	public void remove() {
		from.removeOutgoing(this);
		to.removeIncoming(this);
	}
	
	public boolean isEmpty() {
		return empty;
	}
	
	public State getFrom() {
		return from;
	}

	public State getTo() {
		return to;
	}

	public IAbstractInputItem getInChar() {
		return inChar;
	}
	
	public List<IAbstractOutputItem> getOutput() {
		return output;
	}

	@Override
	public String toString() {
		return from + " -" + inChar + "/" + output + "-> " + to;
	}
}