/**
 * 
 */
package com.zeroturnaround.alvor.lexer.automata;

public interface ICharacterMapper extends IInputToString {
	String map(int c);
}
