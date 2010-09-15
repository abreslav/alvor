package com.zeroturnaround.alvor.sqlparser;

/**
 * A common super-interface for simple LR-parsing stacks ({@link IParserStack}) and GLR-like multi-stacks.
 * 
 * @author abreslav
 *
 */
public interface IParserStackLike {
	/**
	 * If there is an error state on the top of this stack, this method returns it. If there is not, 
	 * the behavior is undefined: it might throw an exception, return null or 
	 * call System.restartWithoutPrompt(NOW) :) 
	 * @return the error state that is on the top of the stack
	 */
	IParserState getErrorOnTop();
	
	/**
	 * @return true iff there is an error state on the top of this stack
	 */
	boolean hasErrorOnTop();
	
	/**
	 * @return true iff the top state is accepting
	 */
	boolean topAccepts();
}
