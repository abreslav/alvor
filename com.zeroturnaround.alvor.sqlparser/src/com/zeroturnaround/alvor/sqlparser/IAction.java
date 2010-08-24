/**
 * 
 */
package com.zeroturnaround.alvor.sqlparser;


import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;

/**
 * Represents a parser action (e.g., SHIFT, REDUCE, GOTO)
 * 
 * @author abreslav
 *
 */
public interface IAction {
	/**
	 * Execute an action
	 * @param inputItem current token
	 * @param stack current stack
	 * @return the resulting stack
	 */
	IParserStack process(IAbstractInputItem inputItem, IParserStack stack);
	
	/**
	 * @return true iff this action (unconditionally) consumes a current token
	 */
	boolean consumes();
	
	/**
	 * @return true iff this action (unconditionally) yields a parsing error 
	 */
	boolean isError();
}
