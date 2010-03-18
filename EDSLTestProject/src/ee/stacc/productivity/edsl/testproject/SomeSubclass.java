package ee.stacc.productivity.edsl.testproject;

import java.sql.Connection;
import java.sql.SQLException;


public class SomeSubclass extends SomeSuperclass {
	private static String STR2 = STR1 + " blaablaa";
	
	@SuppressWarnings("null")
	public static void testInheritedFields() throws SQLException {
		Connection conn=null;
		conn.prepareStatement(STR2);
	}
	
	@SuppressWarnings("null")
	public void testInheritedMethods() throws SQLException {
		Connection conn=null;
		
		SomeSubclass sub1 = new SomeSubclass();
		SomeSuperclass sub2 = new SomeSubclass();
		SomeSuperclass sup1 = new SomeSuperclass();
		
		conn.prepareStatement(getSomeStr());
		conn.prepareStatement(sub1.getSomeStr());
		conn.prepareStatement(sub2.getSomeStr());
		conn.prepareStatement(sup1.getSomeStr());
		
		conn.prepareStatement(SomeSuperclass.getSomeStr(3));
	}
	
	String getSomeStr() {
		return "SomeSubclass.getSomeStr()";
	}
	
	static String getSomeStr(int i) {
		return "SomeSubclass.getSomeStr(int i)";
	}
}
