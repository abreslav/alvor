package com.googlecode.alvor.checkers.sqldynamic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.alvor.checkers.CheckerException;
import com.googlecode.alvor.checkers.HotspotCheckingResult;
import com.googlecode.alvor.checkers.HotspotError;
import com.googlecode.alvor.checkers.HotspotWarningUnsupported;
import com.googlecode.alvor.checkers.IAbstractStringChecker;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.logging.ILog;
import com.googlecode.alvor.common.logging.Logs;
import com.googlecode.alvor.configuration.CheckerConfiguration;
import com.googlecode.alvor.configuration.ProjectConfiguration;
import com.googlecode.alvor.string.samplegen.SampleGenerator;
import com.googlecode.alvor.string.util.AbstractStringSizeCounter;

public abstract class DynamicSQLChecker implements IAbstractStringChecker {
	private static final ILog LOG = Logs.getLog(DynamicSQLChecker.class);
	private static final int SIZE_LIMIT = 10000;
	private int connectionErrorCount = 0;
	
	// connections indexed by hash-code of checker conf
	protected Map<CheckerConfiguration, Connection> connections = new HashMap<CheckerConfiguration, Connection>();

	@Override
	public Collection<HotspotCheckingResult> checkAbstractString(StringHotspotDescriptor descriptor,
			ProjectConfiguration configuration) throws CheckerException {
		
		List<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		
		if (AbstractStringSizeCounter.size(descriptor.getAbstractValue()) > SIZE_LIMIT) {
			results.add(new HotspotWarningUnsupported
				("Dynamic SQL checker: SQL string has too many possible variations", descriptor.getPosition()));
			return results;
		}
		
		Connection conn;
		try {
			conn = this.getConnection(configuration.getDefaultChecker());
		} catch (SQLException e) {
			results.add(new HotspotError("SQL tester connection error: " + e.getMessage(), 
					descriptor.getPosition()));
			return results;
		}
		
		Set<String> concreteStrings = SampleGenerator.getConcreteStrings(descriptor.getAbstractValue());
		for (String s: concreteStrings) {
			assert LOG.message("CON: " + s);
			try {
				this.testSql(s, conn);
			} catch (SQLException e) {
				assert LOG.message("    ERR: " + e.getMessage());
				String message = e.getMessage().trim() + "\nSQL: \n" + s;
				results.add(new HotspotError("SQL test failed  - " + message, descriptor.getPosition()));
				break;
			}
		}

		assert LOG.message("CONCRETE COUNT: " + concreteStrings.size());
		assert LOG.message("____________________________________________");

		return results;
	}
	
	
	protected void testSql(String sql, Connection conn) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.getMetaData();
		}
		finally {
			stmt.close();
		}
	}

	protected Connection createConnectionWithErrorCheck(CheckerConfiguration options) throws SQLException { 
		if (this.connectionErrorCount > 1) {
			// don't waste time for trying to connect anymore
			throw new SQLException("Had several connection errors, not trying anymore");
		}
		
		try {
			return this.createConnection(options.getDriverName(), options.getUrl(), options.getUserName(), options.getPassword());
		}
		catch (ClassNotFoundException e) {
			this.connectionErrorCount++;
			throw new SQLException(e);
		}
		catch (SQLException e) {
			this.connectionErrorCount++;
			throw e;
		}
	}
	
	/**
	 * Can't load right driver in this bundle, because driver library is not available
	 * Each subclass should do "Class.forName(driverName);"
	 * @param driverName
	 */
	protected abstract Connection createConnection(String driverName, String url, String userName, String password) throws ClassNotFoundException, SQLException;
	
	protected Connection getConnection(CheckerConfiguration options) throws CheckerException, SQLException {
		Connection conn = this.connections.get(options);
		if (conn == null) {
			if (options.getDriverName() == null || options.getUrl() == null
					|| options.getUserName() == null || options.getPassword() == null
					|| options.getDriverName().toString().isEmpty()) {
				throw new CheckerException("SQL checker: Test database configuration is not complete", null);
			}
			
			conn = this.createConnectionWithErrorCheck(options);
			this.connections.put(options, conn);
			
		}
		return conn;
	}

	protected boolean isSelectStatement(String sql) {
		return sql.trim().length() >= 6 && sql.trim().substring(0, 6).toLowerCase().equals("select");
	}
}
