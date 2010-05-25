package ee.stacc.productivity.edsl.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;

public class CreateDBHSQL {

	private static final ILog LOG = Logs.getLog(CreateDBHSQL.class);
	
	public static void main(String[] args) {
	    try {
	        Class.forName("org.hsqldb.jdbc.JDBCDriver" );
	    } catch (Exception e) {
	        System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
	        e.printStackTrace();
	        return;
	    }

	    Connection conn = null;
		try {
			runScript(conn);
		} finally {
	    	if (conn != null) {
	    		try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	    	}
	    }
	}

	public static void runScript(Connection conn) {
		try {
			assert LOG.message("DB creation started");
			PreparedStatement commit = conn.prepareStatement("COMMIT");
			
			Reader fileReader = new InputStreamReader(CreateDBHSQL.class.getClassLoader().getResourceAsStream("cache_hsqldb.sql"));
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
					LOG.error("Failed to run \"" + string + "\", " + e.getMessage());
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
