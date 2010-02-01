package ee.stacc.productivity.edsl.main;

import java.sql.SQLException;

import org.eclipse.core.resources.IFile;

public interface ISQLErrorHandler {
	public void handleSQLError(SQLException e, IFile file, int startPosition, int length);
}
