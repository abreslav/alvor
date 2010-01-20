package ee.stacc.productivity.edsl.lexer.automata;

import ee.stacc.productivity.edsl.lexer.ICharacterSet;

public class Transition {
	private final State from;
	private final State to;
	private final ICharacterSet inSet; 
	private final String outStr; 
	
	public Transition(State from, State to, ICharacterSet inSet,
			String outStr) {
		this.from = from;
		this.to = to;
		this.inSet = inSet;
		if (outStr.length() > 1) {
			throw new IllegalArgumentException("Only an empty string or a singe character string are supprted");
		}
		this.outStr = outStr;
	}

	public Transition(State from, State to, ICharacterSet inSet) {
		this(from, to, inSet, "");
	}
	
	public State getFrom() {
		return from;
	}

	public State getTo() {
		return to;
	}

	public ICharacterSet getInSet() {
		return inSet;
	}
	
	public String getOutStr() {
		return outStr;
	}

	@Override
	public String toString() {
		return from + " -" + inSet + "/" + outStr + "-> " + to;
	}
}