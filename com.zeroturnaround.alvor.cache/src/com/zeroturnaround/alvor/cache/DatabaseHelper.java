package com.zeroturnaround.alvor.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
	
	public PreparedStatement prepareAndBind(String sql, Object... arguments) {
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
			return stmt;
		} 
		catch (SQLException e) {
			LOG.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	private Object executeOrQuery(String sql, boolean hasResult, Object... arguments) {
		try {
			PreparedStatement stmt = prepareAndBind(sql, arguments);
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
		Integer result = queryMaybeInteger(sql, arguments);
		if (result == null) {
			throw new IllegalStateException("Query returned no rows");
		}
		else {
			return result;
		}
	}
	
	public Integer queryMaybeInteger(String sql, Object... arguments) {
		try {
			ResultSet rs = query(sql, arguments);
			if (rs.next()) {
				int result = rs.getInt(1);
				rs.close();
				return result;
			}
			else {
				return null;
			}
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void runScript(InputStream stream) {
		
		PreparedStatement commitStmt = null;
		try {
			//assert LOG.message("DB creation started");
			commitStmt = conn.prepareStatement("COMMIT");
			
			Reader fileReader = new InputStreamReader(stream);
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = fileReader.read()) != -1) {
				builder.append((char) c);
			}
			fileReader.close();
			
			String[] split = builder.toString().split(";;;");
			for (String sql : split) {
				if (sql.trim().isEmpty()) {
					continue;
				}
				PreparedStatement stmt = null;
				try {
					stmt = conn.prepareStatement(sql);
					stmt.execute();
					commitStmt.execute();
				} catch (SQLException e) {
					LOG.error("Failed to run '" + sql + "'", e);
					throw new RuntimeException(e);
				} finally {
					if (stmt != null) {
						stmt.close();
					}
				}
			}

			//assert LOG.message("DB creation done");
	    } catch (SQLException e) {
	    	LOG.exception(e);
	    } catch (IOException e) {
			LOG.exception(e);
		} finally {
			if (commitStmt != null) {
				try {
					commitStmt.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public int insertAndGetId(String sql, Object... arguments) {
		try {
			PreparedStatement stmt = prepareAndBind(sql, arguments);
			int rows = stmt.executeUpdate();
			if (rows != 1) {
				throw new IllegalStateException();
			}
	
			ResultSet res;
			res = stmt.getGeneratedKeys();
			if (res.next()) {
				return res.getInt(1);
			}
			else {
				throw new IllegalStateException("Result has no rows");
			}
		}
		catch (SQLException e) {
			LOG.exception(e);
			throw new RuntimeException(e);
		}
	}

	
	public Connection getConnection() {
		return conn;
	}
}
