package com.googlecode.alvor.checkers.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.googlecode.alvor.checkers.sqldynamic.DynamicSQLChecker;

public class DynamicSQLCheckerOracle extends DynamicSQLChecker {
	// TODO: test whether you get "too many open cursors" 

	@Override
	protected Connection createConnection(String driverName, String url, String userName, String password) throws ClassNotFoundException, SQLException {
		Class.forName(driverName);
		return DriverManager.getConnection(url, userName, password);
	}
	

	
	@Override
	protected void testSql(String sql, Connection conn) throws SQLException {
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
			if (stmt != null) {
				stmt.close();
			}
		}
	}
}
