package com.zeroturnaround.alvor.sqlparser;

import java.util.Collection;
import java.util.Collections;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;

/**
 * Error parsing state. Means that there was a parsing error. 
 * Stores the known cause of the error (if any).
 * 
 * @author abreslav
 *
 */
public final class ErrorState implements IParserState {
	private final IParserState fromState;
	private final IAbstractInputItem byInputItem;
	
	/**
	 * @param fromState the state in which the error occurred 
	 * @param byInputItem the input item that caused an error (may be null)
	 */
	public ErrorState(IParserState fromState, IAbstractInputItem byInputItem) {
		this.fromState = fromState;
		this.byInputItem = byInputItem;
	}

	@Override
	public Collection<IAction> getActions(int symbolNumber) {
		return Collections.<IAction>singleton(new AfterErrorAction());
	}
	
	@Override
	public boolean isTerminating() {
		return true;
	}

	@Override
	public boolean isError() {
		return true;
	}
	
	@Override
	public String toString() {
		return "In state " + fromState + " unexpected symbol: " + byInputItem;
	}
	
	/**
	 * If the error was caused by meeting an unexpected item, returns this item
	 */
	public IAbstractInputItem getUnexpectedItem() {
		return byInputItem;
	}
}
