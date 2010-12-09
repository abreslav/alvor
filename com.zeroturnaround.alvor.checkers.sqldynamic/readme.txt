
Forcing MySQL to parse all kind of statements:


import java.sql.SQLException;

import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.ServerPreparedStatement;


public class MyServerPreparedStatement extends ServerPreparedStatement {

	protected MyServerPreparedStatement(MySQLConnection conn, String sql,
			String catalog, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		super(conn, sql, catalog, resultSetType, resultSetConcurrency);
	}

	public static ServerPreparedStatement getInstance(MySQLConnection conn,
			String sql, String catalog, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return ServerPreparedStatement.getInstance(conn, sql, catalog, resultSetType, resultSetConcurrency);
	}

}

----
Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test1", "root", "");
		String sql = "insert into tables1(id, name) values ('e', 'ups')";
		
		assert conn instanceof MySQLConnection;
		
		PreparedStatement stmt = MyServerPreparedStatement.getInstance((MySQLConnection)conn, sql, "test1", 
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);