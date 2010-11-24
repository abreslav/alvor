package com.zeroturnaround.alvor.db;

import java.sql.SQLException;

public abstract class SqlTester {
	protected final String driverName;
	protected final String url;
	protected final String username;
	protected final String password;
	
	public SqlTester(String driverName, String url, String username, String password) {
		this.driverName = driverName;
		this.url = url;
		this.username = username;
		this.password = password;
		
		// NB! can't load the driver here, because classes are not available from here
		// Driver loading should be done in subclass'es constructor
		
		// also can't provide method for connection creation here, for the same reason
	}
	
	public abstract void testSql(String sql) throws SQLException;
	
	protected static boolean isSelectStatement(String sql) {
		return sql.trim().length() >= 6 && sql.trim().substring(0, 6).toLowerCase().equals("select");
	}
}
