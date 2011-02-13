package com.zeroturnaround.alvor.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class DatabaseHelper {
	private final static ILog LOG = Logs.getLog(DatabaseHelper.class);
	private final Connection conn;
	private final Map<String, PreparedStatement> preparedStatments = new HashMap<String, PreparedStatement>();


	public DatabaseHelper(Connection conn) {
		this.conn = conn;
	}
	
	
	public void execute(String sql, Object... arguments) {
		executeOrQuery(sql, false, arguments);
	}
	
	public ResultSet query(String sql, Object... arguments) {
		return (ResultSet)executeOrQuery(sql, true, arguments);
	}
	
	private Object executeOrQuery(String sql, boolean hasResult, Object... arguments) {
		try {
			PreparedStatement stmt = getStatement(sql);
			
			int i = 1;
			for (Object o : arguments) {
				if (o instanceof String) {
					stmt.setString(i, (String) o);
				}
				else if (o instanceof Integer) {
					stmt.setInt(i, (Integer)o);
				}
				else {
					throw new IllegalArgumentException("Unkown argument type: " + o.getClass().getName());
				}
				i++;
			}
			if (hasResult) {
				return stmt.executeQuery();
			} 
			else {
				return stmt.execute();
				//return null;
			}
		} 
		catch (SQLException e) {
			LOG.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	private PreparedStatement getStatement(String sql) {
		try {
			PreparedStatement stmt = this.preparedStatments.get(sql);
			if (stmt == null) {
				stmt = conn.prepareStatement(sql);
				preparedStatments.put(sql, stmt);
			}
			return stmt;
		} 
		catch (SQLException e) {
			LOG.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public int queryInt(String sql, Object... arguments) {
		try {
			ResultSet rs = query(sql, arguments);
			if (rs.next()) {
				int result = rs.getInt(1);
				rs.close();
				return result;
			}
			else {
				throw new IllegalStateException("Query returned no rows");
			}
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
}
