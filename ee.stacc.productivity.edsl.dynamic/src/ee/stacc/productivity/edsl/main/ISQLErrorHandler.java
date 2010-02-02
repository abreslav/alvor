package ee.stacc.productivity.edsl.main;

import org.eclipse.core.resources.IFile;

public interface ISQLErrorHandler {
	public void handleSQLError(String errorMessage, IFile file, int startPosition, int length);
}
