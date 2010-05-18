package ee.stacc.productivity.edsl.cache;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class HSQLDBLayer implements IDBLayer {

	private Connection connection;

	@Override
	public Connection connect() throws SQLException, ClassNotFoundException {
		if (connection == null) {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			
			String url = "jdbc:hsqldb:file:" + getPath() + ";shutdown=true";
			
			// if db is locked, then should connect in server mode (~ debugging mode) 
			if (new File(getPath() + ".lck").exists()) {
				url = "jdbc:hsqldb:hsql://localhost/xdb;shutdown=true";
			}
			connection = DriverManager.getConnection(url, "SA", "");
			
			ResultSet res = connection.getMetaData().getTables(null, null, "FILES", null);
			if (!res.next()) {
				CreateDBHSQL.runScript(connection);
			}
		}
		return connection;
	}

	
	protected abstract String getPath();

	@Override
	public void shutdown() throws SQLException {
		if (connection == null) {
			return;
		}
		connection.close();
	}

}
