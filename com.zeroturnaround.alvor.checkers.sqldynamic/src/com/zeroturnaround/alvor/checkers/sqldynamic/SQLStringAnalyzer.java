package com.zeroturnaround.alvor.checkers.sqldynamic;

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
	private Connection conn = null;
	private String url;
	private String username;
	private String password;
	private int usageCount=0;
	
	public SQLStringAnalyzer(String driverName, String url, String username,
			String password) throws SQLException, ClassNotFoundException {
		
		this.url = url;
		this.username = username;
		this.password = password;
		
		Class.forName (driverName);
		checkConnect();
	}
	
	private void checkConnect() throws SQLException {
		// disconnecting is necessary because of "too many open cursors" error
		if (usageCount % 300 == 0) {
			if (conn != null) {
				conn.close();
			}
			conn = DriverManager.getConnection(url, username, password);
			usageCount = 0;
		}
	}
	
	public void validate(String sql) throws SQLException {
		checkConnect();
		
		PreparedStatement stmt = null;
		try {
			usageCount++;
			stmt = conn.prepareStatement(sql);
			if (url.contains("oracle") || url.contains("odbc")) {
				stmt.getMetaData();
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
}
