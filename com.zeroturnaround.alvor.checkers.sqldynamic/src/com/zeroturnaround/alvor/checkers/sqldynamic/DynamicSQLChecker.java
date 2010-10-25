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
import com.zeroturnaround.alvor.string.Position;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;
import com.zeroturnaround.alvor.string.util.AbstractStringSizeCounter;

public class DynamicSQLChecker implements IAbstractStringChecker {
	private static final ILog LOG = Logs.getLog(DynamicSQLChecker.class);
	private static final int SIZE_LIMIT = 10000;
	
	// analyzers indexed by hash-code of options map
	Map<Integer, SQLStringAnalyzer> analyzers = new HashMap<Integer, SQLStringAnalyzer>();

	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			ISQLErrorHandler errorHandler, Map<String, String> options) throws CheckerException {
		if (descriptors.size() == 0) {
			return;
		}
		
		SQLStringAnalyzer analyzer = this.getAnalyzer(options);
		for (IStringNodeDescriptor descriptor: descriptors) {
			this.checkAbstractString(descriptor, errorHandler, analyzer);
		}
	}
	
	@Override
	public boolean checkAbstractString(IStringNodeDescriptor descriptor,
			ISQLErrorHandler errorHandler, Map<String, String> options) throws CheckerException {
		return checkAbstractString(descriptor, errorHandler, this.getAnalyzer(options));
	}
	
	
	private boolean checkAbstractString(IStringNodeDescriptor descriptor,
			ISQLErrorHandler errorHandler, SQLStringAnalyzer analyzer) {

		int totalConcrete = 0;
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
			List<String> concreteStr = SampleGenerator.getConcreteStrings(descriptor.getAbstractValue());
			totalConcrete += concreteStr.size();

			int duplicates = 0;
			// maps error msg to all concrete strings that cause this message

			for (String s: concreteStr) {
				Integer countSoFar = concretes.get(s);
				duplicates = 0;
				if (countSoFar == null) {
					assert LOG.message("CON: " + s);
					try {
						analyzer.validate(s);
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
				String message = entry.getKey().trim() + "\nSQL: \n" 
				+ entry.getValue();
				errorHandler.handleSQLError("SQL test failed  - " + message, descriptor.getPosition());
			}

			assert LOG.message("DUPLICATES: " + duplicates);
			assert LOG.message("____________________________________________");
			return allOK;
		}
	}

	private SQLStringAnalyzer getAnalyzer(Map<String, String> options) throws CheckerException {
		// give different analyzer for different options
		// first search for cached version
		SQLStringAnalyzer analyzer = this.analyzers.get(options.hashCode());
		
		if (analyzer == null) {
			if (options.get("DBDriverName") == null || options.get("DBUrl") == null
					|| options.get("DBUsername") == null || options.get("DBPassword") == null
					|| options.get("DBDriverName").toString().isEmpty()
					|| options.get("DBDriverName").toString().isEmpty()) {
				throw new CheckerException("SQL checker: Test database configuration is not complete", 
						new Position(options.get("SourceFileName"), 0, 0));
			}
			
			try {
				analyzer = new SQLStringAnalyzer(				
					options.get("DBDriverName").toString(),
					options.get("DBUrl").toString(),
					options.get("DBUsername").toString(),
					options.get("DBPassword").toString());
			} catch (Exception e) {
				throw new CheckerException("SQL checker: can't connect with test database: "
						+ e.getMessage(), new Position(options.get("SourceFileName"), 0, 0));
			}
			
			this.analyzers.put(options.hashCode(), analyzer);
		}
		
		return analyzer;
		
	}
}
