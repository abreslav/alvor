package com.googlecode.alvor.checkers.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.googlecode.alvor.checkers.mysql.internal.MyServerPreparedStatement;
import com.googlecode.alvor.checkers.sqldynamic.DynamicSQLChecker;

public class DynamicSQLCheckerMySQL extends DynamicSQLChecker {
	
	@Override
	protected void testSql(String sql, Connection conn) throws SQLException {
		if (!(conn instanceof com.mysql.jdbc.MySQLConnection)) {
			throw new IllegalArgumentException("Unknown MySQL connection class: " 
					+ conn.getClass().getName());
		}
		
		PreparedStatement stmt = null;
		try {
			// need special treatment, because DML statements are not parsed by prepareStatement
			stmt = MyServerPreparedStatement.getInstance((com.mysql.jdbc.MySQLConnection)conn, sql, 
					conn.getCatalog(), 
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Override
	protected Connection createConnection(String driverName, String url, String userName, String password) throws ClassNotFoundException, SQLException {
		Class.forName(driverName);
		return DriverManager.getConnection(url, userName, password);
	}
	

}
