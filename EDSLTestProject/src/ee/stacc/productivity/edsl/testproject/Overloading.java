package ee.stacc.productivity.edsl.testproject;

import java.sql.Connection;
import java.sql.SQLException;

public class Overloading {
	@SuppressWarnings("null")
	
	private static String getSql(String s) {
		return "str version";
	}
	
	private static String getSql(int i) {
		return "int version";
	}
	
	private static String getSql(int i, String s) {
		return "int,String version";
	}
	
	private static String getSql(int i, int j) {
		return "int,int version";
	}
	
	public static void testFields() throws SQLException {
		Connection conn=null;
		conn.prepareStatement(getSql(1));
		conn.prepareStatement(Overloading.getSql(1));
		conn.prepareStatement(getSql("oo"));
		
		conn.prepareStatement(getSql(1, "oo"));
		conn.prepareStatement(getSql(1, 2));
		
		@ExpectedAbstractValue("int,String version")
		String s1 = getSql(1, "oo");
	}

}
