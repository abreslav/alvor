package com.zeroturnaround.alvor.sqlparser;

/**
 * Produces parsing stacks
 * 
 * @author abreslav
 *
 * @param <S> type of produced stacks
 */
public interface IStackFactory<S> {
	/**
	 * Creates a stack with one state in it
	 * @param state the state to put onto the stack
	 * @return the created stack
	 */
	S newStack(IParserState state);
}
