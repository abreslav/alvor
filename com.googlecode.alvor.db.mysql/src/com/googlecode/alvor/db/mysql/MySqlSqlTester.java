package com.googlecode.alvor.db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.googlecode.alvor.db.SqlTester;
import com.googlecode.alvor.db.mysql.internal.MyServerPreparedStatement;


public class MySqlSqlTester extends SqlTester {
	private Connection conn = null;
	
	public MySqlSqlTester(String driverName, String url, String username,
			String password) throws ClassNotFoundException {
		super(driverName, url, username, password);
		Class.forName(this.driverName);
	}

	@Override
	public void testSql(String sql) throws SQLException {
		if (conn == null) {
			this.conn = DriverManager.getConnection(this.url, this.username, this.password);
		}
		
		
		if (!(this.conn instanceof com.mysql.jdbc.MySQLConnection)) {
			throw new IllegalArgumentException("Unknown MySQL connection class: " 
					+ this.conn.getClass().getName());
		}
		
		// need special treatment, because DML statements are not parsed by prepareStatement
		PreparedStatement stmt = MyServerPreparedStatement.getInstance((com.mysql.jdbc.MySQLConnection)conn, sql, 
				this.conn.getCatalog(), 
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		
		stmt.close();
	}
	
}
