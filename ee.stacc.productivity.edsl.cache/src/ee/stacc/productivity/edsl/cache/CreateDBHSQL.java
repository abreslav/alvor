package ee.stacc.productivity.edsl.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateDBHSQL {
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
			conn = DriverManager.getConnection("jdbc:hsqldb:file:cache;shutdown=true", "SA", "");
			
			PreparedStatement commit = conn.prepareStatement("COMMIT");
			
			FileReader fileReader = new FileReader(new File("db/cache.sql"));
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
					System.out.println("Failed to run \"" + string + "\", " + e.getMessage());
				}
			}

			System.out.println("Done");
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
}
