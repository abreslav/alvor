package ee.stacc.productivity.edsl.lexer.automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractOutputItem;


public class Transition {
	private final State from;
	private final State to;
	private final IAbstractInputItem inChar; 
	private final boolean empty; 
	private final List<IAbstractOutputItem> output; 
	
	public Transition(State from, State to, IAbstractInputItem inChar,
			List<? extends IAbstractOutputItem> output) {
		if (output == null) {
			throw new IllegalArgumentException();
		}
		this.from = from;
		this.to = to;
		this.empty = inChar == null;
		this.inChar = empty ? null : inChar;
		this.output = Collections.unmodifiableList(new ArrayList<IAbstractOutputItem>(output));
	}

	public Transition(State from, State to, IAbstractInputItem inChar) {
		this(from, to, inChar, Collections.<IAbstractOutputItem>emptyList());
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