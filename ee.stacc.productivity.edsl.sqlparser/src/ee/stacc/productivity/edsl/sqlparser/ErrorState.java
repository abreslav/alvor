/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public final class ErrorState implements IParserState {
	private final IParserState fromState;
	private final IAbstractInputItem byInputItem;
	
	public ErrorState(IParserState fromState, IAbstractInputItem byInputItem) {
		this.fromState = fromState;
		this.byInputItem = byInputItem;
	}

	@Override
	public IAction getAction(int symbolNumber) {
		return new AfterErrorAction();
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
	
	public IParserState getFromState() {
		return fromState;
	}
}