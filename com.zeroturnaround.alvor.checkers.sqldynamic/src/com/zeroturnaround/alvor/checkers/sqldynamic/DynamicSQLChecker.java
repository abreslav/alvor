package com.zeroturnaround.alvor.checkers.sqldynamic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.HotspotCheckingResult;
import com.zeroturnaround.alvor.checkers.HotspotError;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.configuration.DataSourceProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.db.SqlTester;
import com.zeroturnaround.alvor.db.generic.GenericSqlTester;
import com.zeroturnaround.alvor.db.mysql.MySqlSqlTester;
import com.zeroturnaround.alvor.db.oracle.OracleSqlTester;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;
import com.zeroturnaround.alvor.string.util.AbstractStringSizeCounter;

public class DynamicSQLChecker implements IAbstractStringChecker {
	private static final ILog LOG = Logs.getLog(DynamicSQLChecker.class);
	private static final int SIZE_LIMIT = 10000;
	
	// analyzers indexed by hash-code of options map
	Map<Integer, SqlTester> testers = new HashMap<Integer, SqlTester>();

	@Override
	public Collection<HotspotCheckingResult> checkAbstractString(StringNodeDescriptor descriptor,
			ProjectConfiguration configuration) throws CheckerException {
		
		if (AbstractStringSizeCounter.size(descriptor.getAbstractValue()) > SIZE_LIMIT) {
			HotspotCheckingResult result = new HotspotError
				("Dynamic SQL checker: SQL string has too many possible variations", descriptor.getPosition());
			return Collections.singletonList(result);
		}
		
		SqlTester tester = this.getAnalyzer(configuration);
		
		List<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		Map<String, Integer> concretes = new HashMap<String, Integer>();
		Map<String, String> errorMap = new HashMap<String, String>();
		List<String> concreteStrings = null;
		
		concreteStrings = SampleGenerator.getConcreteStrings(descriptor.getAbstractValue());
		int duplicates = 0;
		// maps error msg to all concrete strings that cause this message

		for (String s: concreteStrings) {
			Integer countSoFar = concretes.get(s);
			duplicates = 0;
			if (countSoFar == null) {
				assert LOG.message("CON: " + s);
				try {
					tester.testSql(s);
				} catch (SQLException e) {
					assert LOG.message("    ERR: " + e.getMessage());

					String errStrings = errorMap.get(e.getMessage());
					if (errStrings == null) {
						errStrings = s; 
					} else {
						errStrings += ";;;\n" + s;
					}
					errorMap.put(e.getMessage(), errStrings);
				}
				concretes.put(s, 1);
			}
			else {
				concretes.put(s, countSoFar+1);
				duplicates++;
			}
		}

		for (Entry<String, String> entry : errorMap.entrySet()) {
			String message = entry.getKey().trim() + "\nSQL: \n" + entry.getValue();
			results.add(new HotspotError("SQL test failed  - " + message, descriptor.getPosition()));
		}

		assert LOG.message("DUPLICATES: " + duplicates);
		assert LOG.message("____________________________________________");

		return results;
	}
	
	

	private SqlTester getAnalyzer(ProjectConfiguration configuration) throws CheckerException {
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
