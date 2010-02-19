package ee.stacc.productivity.edsl.checkers;


public interface ISQLErrorHandler {
	public void handleSQLError(String errorMessage, IStringNodeDescriptor descriptor);
	public void handleSQLWarning(String message, INodeDescriptor descriptor);
}
