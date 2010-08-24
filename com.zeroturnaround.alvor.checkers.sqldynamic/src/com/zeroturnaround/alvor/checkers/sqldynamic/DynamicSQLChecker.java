package com.zeroturnaround.alvor.checkers.sqldynamic;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.ISQLErrorHandler;
import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;
import com.zeroturnaround.alvor.string.util.AbstractStringSizeCounter;

public class DynamicSQLChecker implements IAbstractStringChecker {
	private static final ILog LOG = Logs.getLog(DynamicSQLChecker.class);
	private static final int SIZE_LIMIT = 10000;

	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			ISQLErrorHandler errorHandler, Map<String, Object> options) {
		if (descriptors.size() == 0) {
			return;
		}
		
		
		SQLStringAnalyzer analyzer = null;
		try {
			analyzer = new SQLStringAnalyzer(				
				options.get("DBDriverName").toString(),
				options.get("DBUrl").toString(),
				options.get("DBUsername").toString(),
				options.get("DBPassword").toString());
		} catch (Exception e) {
			// for position use first pos from the list
			errorHandler.handleSQLError("can't connect with database schema: "
					+ e.getMessage(), descriptors.get(0).getPosition());
			return;
		}
		
		int totalConcrete = 0;
		Map<String, Integer> concretes = new HashMap<String, Integer>();
		
		assert LOG.message("============================================");
		
		for (IStringNodeDescriptor nodeDesc: descriptors) {
			assert LOG.message("ABS: " + nodeDesc.getAbstractValue());
			
			if (AbstractStringSizeCounter.size(nodeDesc.getAbstractValue()) > SIZE_LIMIT) {
				errorHandler.handleSQLWarning("Testing facility: abstract string is too big", 
						nodeDesc.getPosition());
				continue;
			}
			
			List<String> concreteStr = SampleGenerator.getConcreteStrings(nodeDesc.getAbstractValue());
//			LOG.message(conc);
			totalConcrete += concreteStr.size();
			
			int duplicates = 0;
			
			// maps error msg to all concrete strings that cause this message
			Map<String, String> errorMap = new HashMap<String, String>();
			
			for (String s: concreteStr) {
				Integer countSoFar = concretes.get(s);
				duplicates = 0;
				if (countSoFar == null) {
					assert LOG.message("CON: " + s);
					try {
						analyzer.validate(s);
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
			
//			System.out.println(errorMap.keySet());
			
			for (Entry<String, String> entry : errorMap.entrySet()) {
				String message = entry.getKey().trim() + "\nSQL: \n" 
						+ entry.getValue();
				//message = message.substring(0, Math.min(200, message.length()));
				errorHandler.handleSQLError(message, nodeDesc.getPosition());
			}

			
			assert LOG.message("DUPLICATES: " + duplicates);
			assert LOG.message("____________________________________________");
		}
		
		LOG.message("TOTAL ABSTRACT COUNT: " + descriptors.size());
		LOG.message("TOTAL CONCRETE COUNT: " + totalConcrete);
		LOG.message("DIFFERENT CONCRETE COUNT: " + concretes.size());
	}

}
