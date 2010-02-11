package ee.stacc.productivity.edsl.db;

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
	boolean needExecute = false;
	
	public SQLStringAnalyzer() {
		
		//connectToMySQL();
		connectToOracle();
	}
	
	private void connectToOracle() {
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		try {
			Class.forName ("oracle.jdbc.OracleDriver");
			conn = DriverManager.getConnection(url, "compiere", "password");
			this.needExecute = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void connectToMySQL() {
		String url = "jdbc:mysql://localhost:3306/openbravopos";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, "openbravopos", "openbravopos");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void validate(String sql) throws SQLException {
		if (conn == null) {
			return;
		}
		if (this.needExecute) {
			PreparedStatement stmt = conn.prepareStatement(sql.replaceAll("where", "where 1=0 and"));
			stmt.getMetaData();
		} 
		else {
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.getMetaData();
		}
	}
	
	private String simplifySQL(String sql) {
		String prefix = sql.substring(0, sql.toLowerCase().indexOf("where"));
		return prefix + " where 1=0";
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
