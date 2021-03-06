package com.googlecode.alvor.db.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.googlecode.alvor.db.SqlTester;

public class OracleSqlTester extends SqlTester {
	private int connUsageCount = 0;
	private Connection conn = null;
	private int connectionErrorCount = 0;
	
	public OracleSqlTester(String driverName, String url, String username,
			String password) throws ClassNotFoundException {
		super(driverName, url, username, password);
		Class.forName(driverName);
	}
	
	@Override
	public void testSql(String sql) throws SQLException {
		prepareConnection();
		PreparedStatement stmt = null;
		
		try {
			if (isSelectStatement(sql)) {
				stmt = conn.prepareStatement(sql); 
				stmt.getMetaData();
			}
			else {
				// oracle won't actually parse DML statements with prepareStatement
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
			this.connUsageCount++;
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	private void prepareConnection() throws SQLException {
		
		// TODO: create this guard for other drivers also
		if (this.connectionErrorCount > 1) {
			// don't waste time for trying to connect anymore
			throw new SQLException("Had several connection errors, not trying anymore");
		}
		
		// disconnecting seems to be necessary because of "too many open cursors" error
		try {
			if (this.conn == null || this.connUsageCount > 200) {
				if (this.conn != null) {
					this.conn.close();
				}
				connUsageCount = 0;
				this.conn = DriverManager.getConnection(this.url, this.username, this.password);
			}
		} catch (SQLException e) {
			this.connectionErrorCount++;
			throw e;
		}
	}

}
