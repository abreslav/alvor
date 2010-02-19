package ee.stacc.productivity.edsl.checkers.sqldynamic;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
		Hashtable<String, Integer> concretes = new Hashtable<String, Integer>();
		
		LOG.message("============================================");
		
		for (IStringNodeDescriptor nodeDesc: descriptors) {
			LOG.message("ABS: " + nodeDesc.getAbstractValue());
			List<String> concreteStr = SampleGenerator.getConcreteStrings(nodeDesc.getAbstractValue());
//			LOG.message(conc);
			totalConcrete += concreteStr.size();
			
			int duplicates = 0;
			for (String s: concreteStr) {
				Integer soFar = concretes.get(s);
				duplicates = 0;
				if (soFar == null) {
					LOG.message("CON: " + s);
					try {
						analyzer.validate(s);
					} catch (SQLException e) {
						LOG.message("    ERR: " + e.getMessage());
						errorHandler.handleSQLError(e.getMessage().trim() + "\nSQL:\n" + s, nodeDesc);
					}
					
					concretes.put(s, 1);
				}
				else {
					concretes.put(s, soFar+1);
					duplicates++;
				}
			}
			LOG.message("DUPLICATES: " + duplicates);
			LOG.message("____________________________________________");
		}
		
		LOG.message("TOTAL ABSTRACT COUNT: " + descriptors.size());
		LOG.message("TOTAL CONCRETE COUNT: " + totalConcrete);
		LOG.message("DIFFERENT CONCRETE COUNT: " + concretes.size());
	}

}
