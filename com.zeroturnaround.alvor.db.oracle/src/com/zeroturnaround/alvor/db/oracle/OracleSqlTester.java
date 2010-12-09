package com.zeroturnaround.alvor.db.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.zeroturnaround.alvor.db.SqlTester;

public class OracleSqlTester extends SqlTester {
	private int connUsageCount = 0;
	private Connection conn = null;
	
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
		// disconnecting seems to be necessary because of "too many open cursors" error
		if (this.conn == null || this.connUsageCount > 200) {
			if (this.conn != null) {
				this.conn.close();
			}
			connUsageCount = 0;
			this.conn = DriverManager.getConnection(this.url, this.username, this.password);
		}
	}

}
