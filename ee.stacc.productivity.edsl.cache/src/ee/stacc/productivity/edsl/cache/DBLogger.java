package ee.stacc.productivity.edsl.cache;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBLogger {

	public static final DBLogger INSTANCE = new DBLogger();
	private Connection connection;
	
	private DBLogger() {
	    try {
	        Class.forName("org.hsqldb.jdbc.JDBCDriver" );
	        connection = DriverManager.getConnection("jdbc:hsqldb:file:/media/data/work/STACC/ws/ee.stacc.productivity.edsl.cache/testdb;shutdown=true;ifExists=true", "SA", "");
	    } catch (Exception e) {
	        System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
	        throw new IllegalStateException(e);
	    }
	}
	
	protected void finalize() throws Throwable {
		connection.close();		
	};

	public void addFile(String fileName) {
		try {
			int fileId = findFileId(fileName);
			if (fileId >= 0) {
				return;
			}
			connection.prepareCall("INSERT INTO Files(name) VALUES ('" + fileName + "')").execute();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private int findFileId(String fileName) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("SELECT id FROM Files WHERE name = '" + fileName + "'");
		statement.execute();
		ResultSet resultSet = statement.getResultSet();
		if (resultSet.next()) {
			return resultSet.getInt(1);
		} else {
			return -1;
		}
	}

	int count = 0;
	public void addSourceRange(String fileName, int startPosition, int length) {
		addFile(fileName);		
		try {
			connection.prepareCall("INSERT INTO SourceRanges(file, start, length) VALUES ((SELECT id FROM Files WHERE name = '" + fileName + "'), " + startPosition + ", " + length + ")").execute();
			System.out.println(count++);
		} catch (SQLException e) {
			// duplicate
		}
	}

	public void addStringLiteral(String fileName,
			int startPosition, int length, String literalValue,
			String escapedValue) {
		addFile(fileName);
		addSourceRange(fileName, startPosition, length);
		try {
			connection.prepareCall("INSERT INTO StringLiterals(literalValue, escapeValue, sourceRange) VALUES (" +
						"'" + literalValue + "', " + 
						"'" + escapedValue + "', " + 
						"(SELECT id FROM SourceRanges " + // <---
						"SELECT id FROM Files WHERE name = '" + fileName + "')" + 
					")"
			).execute();
			System.out.println(count++);
		} catch (SQLException e) {
			// duplicate
		}
	}
	
}
