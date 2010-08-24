/**
 * 
 */
package com.zeroturnaround.alvor.lexer.automata;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;

public interface IInputToString {
	String toString(IAbstractInputItem item);
}
