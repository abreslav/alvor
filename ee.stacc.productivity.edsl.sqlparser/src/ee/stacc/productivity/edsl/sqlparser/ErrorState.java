/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.Collection;
import java.util.Collections;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public final class ErrorState implements IParserState {
	private final IParserState fromState;
	private final IAbstractInputItem byInputItem;
	
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
	
	public IAbstractInputItem getUnexpectedItem() {
		return byInputItem;
	}
}