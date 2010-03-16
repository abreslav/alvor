package ee.stacc.productivity.edsl.cache;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
	public static void main(String[] args) {
		System.out.println("a");
	    try {
	        Class.forName("org.hsqldb.jdbc.JDBCDriver" );
	    } catch (Exception e) {
	        System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
	        e.printStackTrace();
	        return;
	    }

	    System.out.println("b");
	    Connection c = null;
		try {
			c = DriverManager.getConnection("jdbc:hsqldb:file:testdb;shutdown=true;hsqldb.log_data=false", "SA", "");
			System.out.println("c");
	    
//	    	c.prepareCall("DROP TABLE t").execute();
//		    c.prepareCall("CREATE CACHED TABLE t (id INTEGER, name LONGVARCHAR)").execute();
		    c.prepareCall("DELETE FROM t").execute();
	    	Statement s = c.createStatement();
	    	long t = System.currentTimeMillis();
	    	for (int i = 0; i < 100; i++) {
	    		s.addBatch("INSERT INTO t VALUES (1, 'asd')");
			}
	    	System.out.println(System.currentTimeMillis() - t);
	    	t = System.currentTimeMillis();
	    	s.executeBatch();
	    	t = System.currentTimeMillis() - t;
	    	System.out.println(t);
	    	
	    	System.out.println("sdf");
		    CallableStatement statement = c.prepareCall("SELECT COUNT(*) FROM t");
		    long tm = System.currentTimeMillis();
		    boolean execute = statement.execute();
		    tm = System.currentTimeMillis() - tm;
		    System.out.println(tm);
			if (!execute) {
				System.out.println("fail");
			} else {
				ResultSet resultSet = statement.getResultSet();
				for (;!resultSet.isLast();) {
					resultSet.next();
					int id = resultSet.getInt(1);
//					String name = resultSet.getString("name");
					System.out.println(id);// + " " + name); 
				}
			}
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    } finally { 
	    	if (c != null) {
	    		try {
					c.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	    	}
	    }
		
	    
	}
}
