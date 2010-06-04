package ee.stacc.productivity.edsl.checkers.sqldynamic;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.string.samplegen.SampleGenerator;

public class DynamicSQLChecker implements IAbstractStringChecker {
	private static final ILog LOG = Logs.getLog(DynamicSQLChecker.class);

	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			ISQLErrorHandler errorHandler, Map<String, Object> options) {
		SQLStringAnalyzer analyzer = new SQLStringAnalyzer(				
				options.get("DBDriverName").toString(),
				options.get("DBUrl").toString(),
				options.get("DBUsername").toString(),
				options.get("DBPassword").toString());

		int totalConcrete = 0;
		Map<String, Integer> concretes = new HashMap<String, Integer>();
		
		LOG.message("============================================");
		
		for (IStringNodeDescriptor nodeDesc: descriptors) {
			LOG.message("ABS: " + nodeDesc.getAbstractValue());
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
					LOG.message("CON: " + s);
					try {
						analyzer.validate(s);
					} catch (SQLException e) {
						LOG.message("    ERR: " + e.getMessage());
//						errorHandler.handleSQLError(e.getMessage().trim() + "\nSQL:\n" + s, nodeDesc.getPosition());
						
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

			
			LOG.message("DUPLICATES: " + duplicates);
			LOG.message("____________________________________________");
		}
		
		LOG.message("TOTAL ABSTRACT COUNT: " + descriptors.size());
		LOG.message("TOTAL CONCRETE COUNT: " + totalConcrete);
		LOG.message("DIFFERENT CONCRETE COUNT: " + concretes.size());
	}

}
