package ee.stacc.productivity.edsl.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Responsible for parsing and validating given SQL string
 */
public class SQLStringAnalyzer {
	Connection conn;
	String url = "jdbc:mysql://localhost:3306/openbravopos";
	
	public SQLStringAnalyzer() {
		// connect to database
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, "openbravopos", "openbravopos");
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public void validate(String sql) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.getMetaData();
	}
	
	/*
	public SQLStructure analyzeAndReturn(String sql) {
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
//			System.out.println("CHECKED: " + sql);
			return new SQLStructure(stmt.getMetaData(), stmt.getParameterMetaData());
		}
		catch (SQLException e) {
			System.err.println("ERR SQL: " + sql);
			String errorMsg = e.getMessage().replace("\n", "; ");
			System.err.println("SQL ERR: " + errorMsg);
			return new SQLStructure(e);
		}
	}
	*/
}
