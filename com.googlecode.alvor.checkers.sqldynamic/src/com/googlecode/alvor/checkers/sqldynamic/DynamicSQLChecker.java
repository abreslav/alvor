package com.googlecode.alvor.checkers.sqldynamic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.googlecode.alvor.configuration.DataSourceProperties;
import com.googlecode.alvor.configuration.ProjectConfiguration;
import com.googlecode.alvor.db.SqlTester;
import com.googlecode.alvor.db.generic.GenericSqlTester;
import com.googlecode.alvor.db.mysql.MySqlSqlTester;
import com.googlecode.alvor.db.oracle.OracleSqlTester;
import com.googlecode.alvor.string.samplegen.SampleGenerator;
import com.googlecode.alvor.string.util.AbstractStringSizeCounter;

public class DynamicSQLChecker implements IAbstractStringChecker {
	private static final ILog LOG = Logs.getLog(DynamicSQLChecker.class);
	private static final int SIZE_LIMIT = 10000;
	
	// testers indexed by hash-code of options map
	private Map<Integer, SqlTester> testers = new HashMap<Integer, SqlTester>();

	@Override
	public Collection<HotspotCheckingResult> checkAbstractString(StringHotspotDescriptor descriptor,
			ProjectConfiguration configuration) throws CheckerException {
		
		if (AbstractStringSizeCounter.size(descriptor.getAbstractValue()) > SIZE_LIMIT) {
			HotspotCheckingResult result = new HotspotWarningUnsupported
				("Dynamic SQL checker: SQL string has too many possible variations", descriptor.getPosition());
			return Collections.singletonList(result);
		}
		
		SqlTester tester = this.getTester(configuration);
		
		List<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		Set<String> concreteStrings = SampleGenerator.getConcreteStrings(descriptor.getAbstractValue());
		for (String s: concreteStrings) {
			assert LOG.message("CON: " + s);
			try {
				tester.testSql(s);
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
	
	

	private SqlTester getTester(ProjectConfiguration configuration) throws CheckerException {
		// give different analyzer for different options
		// first search for cached version
		
		DataSourceProperties options = configuration.getDefaultDataSource();
		
		SqlTester tester = this.testers.get(options.hashCode());
		
		
		if (tester == null) {
			if (options.getDriverName() == null || options.getUrl() == null
					|| options.getUserName() == null || options.getPassword() == null
					|| options.getDriverName().toString().isEmpty()) {
				throw new CheckerException("SQL checker: Test database configuration is not complete", 
						null);
			}
			
			try {
				if (options.getDriverName().contains("oracle")) {
					tester = new OracleSqlTester(				
							options.getDriverName().toString(),
							options.getUrl().toString(),
							options.getUserName().toString(),
							options.getPassword().toString());
				}
				else if (options.getDriverName().contains("mysql")) {
					tester = new MySqlSqlTester(				
							options.getDriverName().toString(),
							options.getUrl().toString(),
							options.getUserName().toString(),
							options.getPassword().toString());
				}
				else {
					tester = new GenericSqlTester(				
							options.getDriverName().toString(),
							options.getUrl().toString(),
							options.getUserName().toString(),
							options.getPassword().toString());
				}
			} catch (Exception e) {
				LOG.exception(e);
				throw new CheckerException("SQL checker: can't connect with test database: "
						+ e.getMessage(), null);
			}
			
			this.testers.put(options.hashCode(), tester);
		}
		
		return tester;
	}
}
