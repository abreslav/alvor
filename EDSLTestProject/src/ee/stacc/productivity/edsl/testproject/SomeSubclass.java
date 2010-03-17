package ee.stacc.productivity.edsl.testproject;

import java.sql.Connection;
import java.sql.SQLException;


public class SomeSubclass extends SomeSuperclass {
	private static String STR2 = STR1 + " blaablaa";
	
	@SuppressWarnings("null")
	public static void testFields() throws SQLException {
		Connection conn=null;
		conn.prepareStatement(STR2);
	}
}
