package ee.stacc.productivity.edsl.cache;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DerbyDBLayer implements IDBLayer {

	private Connection connection;

	@Override
	public Connection connect() throws SQLException, ClassNotFoundException {
		if (connection == null) {
	        Class.forName("org.apache.derby.jdbc.EmbeddedDriver" );
	        connection = DriverManager.getConnection("jdbc:derby:directory:/media/data/work/STACC/cache_ws/ee.stacc.productivity.edsl.cache/test", "SA", "");
		}
		return connection;
	}

	@Override
	public void shutdown() throws SQLException {
		if (connection == null) {
			return;
		}
		connection.close();

		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if (!"XJ015".equals(e.getSQLState())) {
				throw e;
			}
		}
	}

}
