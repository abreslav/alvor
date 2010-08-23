package ee.stacc.productivity.edsl.cache;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HSQLDBLayer implements IDBLayer {

	private Connection connection;

	@Override
	public Connection connect() throws SQLException, ClassNotFoundException {
		if (connection == null) {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			
			String fileUrl = "jdbc:hsqldb:file:" + getPath() + ";shutdown=true";
			String serverUrl = "jdbc:hsqldb:hsql://localhost/xdb";
			
			// if db is locked, then assume that server is running and connect in server mode (~ debugging mode)
			File lockFile = new File(getPath() + ".lck"); 
			if (lockFile.exists()) {
				try {
					connection = DriverManager.getConnection(serverUrl, "SA", "");
				} catch (SQLException e) {
					// Seems that server is not running, probably the lock is leftover from a crash
					// HSQL is now supposed to do some repair on connect
					connection = DriverManager.getConnection(fileUrl, "SA", "");
				}
			}
			else {
				connection = DriverManager.getConnection(fileUrl, "SA", "");
			}
			
			ResultSet res = connection.getMetaData().getTables(null, null, "FILES", null);
			if (!res.next()) {
				CreateDBHSQL.runScript(connection);
			}
		}
		return connection;
	}

	
	protected String getPath() {
		return EDSLCachePlugin.getDefault().getStateLocation().append("/cache").toPortableString();
	}

	@Override
	public void shutdown() throws SQLException {
		if (connection == null) {
			return;
		}
		connection.close();
	}

}
