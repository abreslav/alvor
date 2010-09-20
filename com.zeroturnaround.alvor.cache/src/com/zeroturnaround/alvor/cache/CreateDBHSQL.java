package com.zeroturnaround.alvor.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class CreateDBHSQL {

	private static final ILog LOG = Logs.getLog(CreateDBHSQL.class);
	
	/**
	 * Used for creating database manually
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
	    try {
	        Class.forName("org.hsqldb.jdbc.JDBCDriver" );
	    } catch (Exception e) {
	    	LOG.error("ERROR: failed to load HSQLDB JDBC driver.", e);
	        return;
	    }

	    // assuming server mode 
	    String url = "jdbc:hsqldb:hsql://localhost/xdb;shutdown=true";
	    Connection conn = DriverManager.getConnection(url, "SA", "");
		try {
			runScript(conn);
		} finally {
	    	if (conn != null) {
	    		try {
					conn.close();
				} catch (SQLException e) {
					LOG.error("running cache creation script", e);
				}
	    	}
	    }
	}

	public static void runScript(Connection conn) {
		try {
			assert LOG.message("DB creation started");
			PreparedStatement commit = conn.prepareStatement("COMMIT");
			
			Reader fileReader = new InputStreamReader(CreateDBHSQL.class.getClassLoader().getResourceAsStream("db/cache_hsqldb.sql"));
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = fileReader.read()) != -1) {
				builder.append((char) c);
			}
			fileReader.close();
			
			String[] split = builder.toString().split(";;;");
			for (String string : split) {
				try {
					conn.prepareStatement(string).execute();
					commit.execute();
				} catch (SQLException e) {
					LOG.error("Failed to run \"" + string, e);
				}
			}

			assert LOG.message("DB creation done");
	    } catch (SQLException e) {
	    	LOG.exception(e);
	    } catch (FileNotFoundException e) {
	    	LOG.exception(e);
		} catch (IOException e) {
			LOG.exception(e);
		}
	}
}
