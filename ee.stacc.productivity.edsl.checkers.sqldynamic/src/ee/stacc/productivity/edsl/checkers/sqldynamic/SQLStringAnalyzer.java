package ee.stacc.productivity.edsl.checkers.sqldynamic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Responsible for parsing and validating given SQL string
 * 
 * Some DB drivers won't actually parse query at "parseStatement"
 */
public class SQLStringAnalyzer {
	Connection conn;
	
	public SQLStringAnalyzer(String driverName, String url, String username,
			String password) {
		
		try {
			Class.forName (driverName);
			conn = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void validate(String sql) throws SQLException {
		if (conn == null) {
			return;
		}
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.getMetaData();
	}
}
