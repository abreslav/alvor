package com.googlecode.alvor.lexer.alphabet;

import com.googlecode.alvor.lexer.automata.IOutputItemInterpreter;

/**
 * An "output command" for transducer: tells the interpreter ({@link IOutputItemInterpreter}) 
 * to remember the current input character in an internal buffer. 
 * 
 * @author abreslav
 *
 */
public class PushInput implements IAbstractOutputItem {
	public static PushInput INSTANCE = new PushInput();
	
	private PushInput() {}
}
