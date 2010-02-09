/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import ee.stacc.productivity.edsl.sqlparser.LRParser.State;

public final class ErrorState implements IParserState {
	private final IParserState fromState;
	private final int bySymbol;
	
	public ErrorState(State fromState, int bySymbol) {
		this.fromState = fromState;
		this.bySymbol = bySymbol;
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
		return "In state " + fromState + " unexpected symbol: " + bySymbol;
	}
	
	public int getUnexpectedSymbol() {
		return bySymbol;
	}
	
	public IParserState getFromState() {
		return fromState;
	}
}