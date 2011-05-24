package com.googlecode.alvor.checkers.postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.googlecode.alvor.checkers.sqldynamic.DynamicSQLChecker;

public class DynamicSQLCheckerPostgreSQL extends DynamicSQLChecker {

	@Override
	protected Connection createConnection(String driverName, String url, String userName, String password) throws ClassNotFoundException, SQLException {
		Class.forName(driverName);
		return DriverManager.getConnection(url, userName, password);
	}
	
	
	
	@Override
	protected void testSql(String sql, Connection conn) throws SQLException {
		super.testSql(sql, conn);
	}

}
