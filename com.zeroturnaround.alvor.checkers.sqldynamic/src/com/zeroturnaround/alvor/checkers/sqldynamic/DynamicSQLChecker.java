package com.zeroturnaround.alvor.checkers.sqldynamic;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.ISQLErrorHandler;
import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.db.SqlTester;
import com.zeroturnaround.alvor.db.generic.GenericSqlTester;
import com.zeroturnaround.alvor.db.mysql.MySqlSqlTester;
import com.zeroturnaround.alvor.db.oracle.OracleSqlTester;
import com.zeroturnaround.alvor.string.Position;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;
import com.zeroturnaround.alvor.string.util.AbstractStringSizeCounter;

public class DynamicSQLChecker implements IAbstractStringChecker {
	private static final ILog LOG = Logs.getLog(DynamicSQLChecker.class);
	private static final int SIZE_LIMIT = 10000;
	
	// analyzers indexed by hash-code of options map
	Map<Integer, SqlTester> testers = new HashMap<Integer, SqlTester>();

	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			ISQLErrorHandler errorHandler, Map<String, String> options) throws CheckerException {
		if (descriptors.size() == 0) {
			return;
		}
		
		SqlTester tester = this.getAnalyzer(options);
		for (IStringNodeDescriptor descriptor: descriptors) {
			this.checkAbstractString(descriptor, errorHandler, tester);
		}
	}
	
	@Override
	public boolean checkAbstractString(IStringNodeDescriptor descriptor,
			ISQLErrorHandler errorHandler, Map<String, String> options) throws CheckerException {
		return checkAbstractString(descriptor, errorHandler, this.getAnalyzer(options));
	}
	
	
	private boolean checkAbstractString(IStringNodeDescriptor descriptor,
			ISQLErrorHandler errorHandler, SqlTester tester) {

		boolean allOK = true;
		Map<String, Integer> concretes = new HashMap<String, Integer>();

		assert LOG.message("DYN CHECK ABS: " + descriptor.getAbstractValue());
		
		// FIXME if AS contains repetition then check but return false

		Map<String, String> errorMap = new HashMap<String, String>();
		if (AbstractStringSizeCounter.size(descriptor.getAbstractValue()) > SIZE_LIMIT) {
			errorHandler.handleSQLWarning("Dynamic SQL checker: SQL string has too many possible variations", 
					descriptor.getPosition());
			return false;
		} 
		else { 
			List<String> concreteStrings = null;
			try {
				concreteStrings = SampleGenerator.getConcreteStrings(descriptor.getAbstractValue());
			} catch (Exception e) {
				errorHandler.handleSQLError("Sample generation failed: " + e.getMessage()
						+ ", str=" + descriptor.getAbstractValue(), descriptor.getPosition());
				return false;
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
						allOK = false;
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
				errorHandler.handleSQLError("SQL test failed  - " + message, descriptor.getPosition());
			}

			assert LOG.message("DUPLICATES: " + duplicates);
			assert LOG.message("____________________________________________");
			return allOK;
		}
	}

	private SqlTester getAnalyzer(Map<String, String> options) throws CheckerException {
		// give different analyzer for different options
		// first search for cached version
		SqlTester tester = this.testers.get(options.hashCode());
		
		if (tester == null) {
			if (options.get("DBDriverName") == null || options.get("DBUrl") == null
					|| options.get("DBUsername") == null || options.get("DBPassword") == null
					|| options.get("DBDriverName").toString().isEmpty()
					|| options.get("DBDriverName").toString().isEmpty()) {
				throw new CheckerException("SQL checker: Test database configuration is not complete", 
						new Position(options.get("SourceFileName"), 0, 0));
			}
			
			try {
				if (options.get("DBDriverName").contains("oracle")) {
					tester = new OracleSqlTester(				
							options.get("DBDriverName").toString(),
							options.get("DBUrl").toString(),
							options.get("DBUsername").toString(),
							options.get("DBPassword").toString());
				}
				else if (options.get("DBDriverName").contains("mysql")) {
					tester = new MySqlSqlTester(				
							options.get("DBDriverName").toString(),
							options.get("DBUrl").toString(),
							options.get("DBUsername").toString(),
							options.get("DBPassword").toString());
				}
				else {
					tester = new GenericSqlTester(				
							options.get("DBDriverName").toString(),
							options.get("DBUrl").toString(),
							options.get("DBUsername").toString(),
							options.get("DBPassword").toString());
				}
			} catch (Exception e) {
				LOG.exception(e);
				throw new CheckerException("SQL checker: can't connect with test database: "
						+ e.getMessage(), new Position(options.get("SourceFileName"), 0, 0));
			}
			
			this.testers.put(options.hashCode(), tester);
		}
		
		return tester;
	}
}
