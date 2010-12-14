package com.zeroturnaround.alvor.sqlparser.framework;

public interface IAbstractablePredicate<S, C> {
	
	S transition(S state, C character);
	
	/**
	 * Detects error states
	 * @param state the state that is examined
	 * @return an IError object describing the error, 
	 *         or {@value IError#NO_ERROR} if no error in this state
	 */
	IError getError(S state);
	
	S getInitialState();	
}

