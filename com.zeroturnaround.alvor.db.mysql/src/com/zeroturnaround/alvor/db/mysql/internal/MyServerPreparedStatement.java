package com.zeroturnaround.alvor.db.mysql.internal;


import java.sql.SQLException;

import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.ServerPreparedStatement;


public class MyServerPreparedStatement extends ServerPreparedStatement {

	protected MyServerPreparedStatement(MySQLConnection conn, String sql,
			String catalog, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		super(conn, sql, catalog, resultSetType, resultSetConcurrency);
	}

	public static ServerPreparedStatement getInstance(MySQLConnection conn,
			String sql, String catalog, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return ServerPreparedStatement.getInstance(conn, sql, catalog, resultSetType, resultSetConcurrency);
	}

}