package com.googlecode.alvor.sqlparser.framework;

public interface IAbstractablePredicate<S, C> {
	
	S transition(S stack, C character);
	
	/**
	 * Detects error states
	 * @param stack the state that is examined
	 * @return an IError object describing the error, 
	 *         or {@value IError#NO_ERROR} if no error in this state
	 */
	IError getError(S stack);
	
	S getInitialStack();	
}

