package ee.stacc.productivity.edsl.lexer.automata;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractOutputItem;

public class SimpleOutput implements IAbstractOutputItem {
	private final char c;

	public SimpleOutput(char c) {
		this.c = c;
	}
	
	public char getOutChar() {
		return c;
	}
	
	@Override
	public String toString() {
		return "" + c;
	}
}
