package com.googlecode.alvor.lexer.automata;

import com.googlecode.alvor.lexer.alphabet.IAbstractOutputItem;

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
