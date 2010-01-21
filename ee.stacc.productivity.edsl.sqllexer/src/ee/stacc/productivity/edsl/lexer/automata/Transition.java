package ee.stacc.productivity.edsl.lexer.automata;


public class Transition {
	private final State from;
	private final State to;
	private final char inChar; 
	private final boolean empty; 
	private final String outStr; 
	
	public Transition(State from, State to, Character inChar,
			String outStr) {
		this.from = from;
		this.to = to;
		this.empty = inChar == null;
		this.inChar = empty ? 0 : inChar;
		
		if (outStr.length() > 1) {
			throw new IllegalArgumentException("Only an empty string or a singe character string are supprted");
		}
		this.outStr = outStr;
	}

	public Transition(State from, State to, Character inChar) {
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

	public char getInChar() {
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