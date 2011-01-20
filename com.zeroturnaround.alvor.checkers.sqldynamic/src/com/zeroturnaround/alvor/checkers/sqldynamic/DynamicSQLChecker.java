package com.zeroturnaround.alvor.checkers.sqldynamic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zeroturnaround.alvor.checkers.AbstractStringCheckingResult;
import com.zeroturnaround.alvor.checkers.AbstractStringError;
import com.zeroturnaround.alvor.checkers.CheckerException;
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
	public Collection<AbstractStringCheckingResult> checkAbstractStrings(List<StringNodeDescriptor> descriptors,
			ProjectConfiguration configuration) throws CheckerException {
		SqlTester tester = this.getAnalyzer(configuration);
		List<AbstractStringCheckingResult> result = new ArrayList<AbstractStringCheckingResult>();
		
		for (StringNodeDescriptor descriptor: descriptors) {
			result.addAll(this.checkAbstractString(descriptor, tester));
		}
		return result;
	}
	
	@Override
	public Collection<AbstractStringCheckingResult> checkAbstractString(StringNodeDescriptor descriptor,
			ProjectConfiguration configuration) throws CheckerException {
		return checkAbstractString(descriptor, this.getAnalyzer(configuration));
	}
	
	
	private Collection<AbstractStringCheckingResult> checkAbstractString(StringNodeDescriptor descriptor,
			SqlTester tester) {

		List<AbstractStringCheckingResult> errors = new ArrayList<AbstractStringCheckingResult>();
		Map<String, Integer> concretes = new HashMap<String, Integer>();

		assert LOG.message("DYN CHECK ABS: " + descriptor.getAbstractValue());
		
		// FIXME if AS contains repetition then check but return false

		Map<String, String> errorMap = new HashMap<String, String>();
		if (AbstractStringSizeCounter.size(descriptor.getAbstractValue()) > SIZE_LIMIT) {
			errors.add(new AbstractStringError("Dynamic SQL checker: SQL string has too many possible variations", 
					descriptor.getPosition()));
		} 
		else { 
			List<String> concreteStrings = null;
			try {
				concreteStrings = SampleGenerator.getConcreteStrings(descriptor.getAbstractValue());
			} catch (Exception e) {
				errors.add(new AbstractStringError("Sample generation failed: " + e.getMessage()
						+ ", str=" + descriptor.getAbstractValue(), descriptor.getPosition()));
			}

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
				errors.add(new AbstractStringError("SQL test failed  - " + message, descriptor.getPosition()));
			}

			assert LOG.message("DUPLICATES: " + duplicates);
			assert LOG.message("____________________________________________");
		}
		return errors;
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
