package ee.stacc.productivity.edsl.lexer.automata;


public class Transition {
	private final State from;
	private final State to;
	// -1 for EOF
	private final int inChar; 
	private final boolean empty; 
	private final String outStr; 
	
	public Transition(State from, State to, Integer inChar,
			String outStr) {
		this.from = from;
		this.to = to;
		this.empty = inChar == null;
		this.inChar = empty ? 0 : inChar;
		this.outStr = outStr;
	}

	public Transition(State from, State to, Integer inChar) {
		this(from, to, inChar, "");
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

	public int getInChar() {
		return inChar;
	}
	
	public String getOutStr() {
		return outStr;
	}

	@Override
	public String toString() {
		return from + " -" + inChar + "/" + outStr + "-> " + to;
	}
}