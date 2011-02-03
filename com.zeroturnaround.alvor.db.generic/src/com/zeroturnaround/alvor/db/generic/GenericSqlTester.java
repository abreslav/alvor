package com.zeroturnaround.alvor.db.generic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.zeroturnaround.alvor.db.SqlTester;

public class GenericSqlTester extends SqlTester {
	private Connection conn = null;

	public GenericSqlTester(String driverName, String url, String username,
			String password) throws ClassNotFoundException {
		super(driverName, url, username, password);
		Class.forName(driverName);
	}
	
	@Override
	public void testSql(String sql) throws SQLException {
		if (this.conn == null) {
			conn = DriverManager.getConnection(this.url, this.username, this.password);
		}
		
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.getMetaData();
		}
		finally {
			stmt.close();
		}
	}
}
