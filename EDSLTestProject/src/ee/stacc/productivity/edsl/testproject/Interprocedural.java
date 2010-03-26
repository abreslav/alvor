package ee.stacc.productivity.edsl.testproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Interprocedural {
	String addWhere(String sql) {
	  return sql + " WHERE a > b";
	}
	
	void doSelect() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = conn.prepareStatement(addWhere("select * from tab"));
	}
}
