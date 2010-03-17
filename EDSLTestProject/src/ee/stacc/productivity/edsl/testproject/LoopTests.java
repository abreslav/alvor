package ee.stacc.productivity.edsl.testproject;

import java.sql.Connection;
import java.sql.SQLException;

public class LoopTests {
	
	public static void testJavadocWorkaround() throws SQLException {
		Connection conn=null;
		conn.prepareStatement("select * from tab where id in "
				+ EArvedStringUtil.getQuestionMarksForQuery(10));
	}

}
