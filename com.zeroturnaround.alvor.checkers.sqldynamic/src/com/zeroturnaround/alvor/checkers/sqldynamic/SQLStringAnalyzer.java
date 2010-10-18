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
	private int testCount=0;
	private String driverName;
	
	public SQLStringAnalyzer(String driverName, String url, String username,
			String password) throws SQLException, ClassNotFoundException {
		
		this.url = url;
		this.username = username;
		this.password = password;
		this.driverName = driverName;
		
		Class.forName (driverName);
		checkConnect();
	}
	
	private void checkConnect() throws SQLException {
		// disconnecting is necessary because of "too many open cursors" error
		if (testCount % 100 == 0) {
			if (conn != null) {
				conn.close();
			}
			conn = DriverManager.getConnection(url, username, password);
			testCount = 0;
		}
	}
	
	public void validate(String sql) throws SQLException {
		checkConnect();
		
		if (url.contains("oracle")) {
			testOracleSQL(sql);
		}
		else {
			testNormalEngineSQL(sql);
		}
		testCount++;
	}
	public void testNormalEngineSQL(String sql) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			// TODO there may be other engines besides oracle that require stmt.getMetaData()
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	public void testOracleSQL(String sql) throws SQLException {
		boolean isSelect = sql.substring(0, 6).toLowerCase().equals("select");
		PreparedStatement stmt = null;
		try {
			if (isSelect) {
				System.out.println("##### SELECT");
				stmt = conn.prepareStatement(sql); 
				stmt.getMetaData();
			}
			else {
				// oracle wont parse DML statement without executing
				// so i'll parse them explicitly
				String quotedSql = sql.replace("'", "''").replace("?", "null");
				stmt = conn.prepareCall(
					"declare " +
					"    c integer;" +
					"    stmt varchar2(4000) := '" + quotedSql +  "';" +
					"begin " +
					"    c := dbms_sql.open_cursor;" +
					"    dbms_sql.parse(c,stmt,dbms_sql.native);" +
					"    dbms_sql.close_cursor(c);" +
					"exception" +
					"    when others then" +
					"    begin" +
					"        dbms_sql.close_cursor(c);" +
					"        raise;" +
					"    end;" +
					"end;"); 
				stmt.execute();
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	
	public String getUrl() {
		return url;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getDriverName() {
		return driverName;
	}
}
