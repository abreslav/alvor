package ee.stacc.productivity.edsl.checkers;

import ee.stacc.productivity.edsl.string.IPosition;


public interface ISQLErrorHandler {
	public void handleSQLError(String errorMessage, IPosition position);
	public void handleSQLWarning(String message, IPosition position);
}
