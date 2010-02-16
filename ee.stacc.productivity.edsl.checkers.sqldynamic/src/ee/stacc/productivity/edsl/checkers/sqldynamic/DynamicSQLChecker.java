package ee.stacc.productivity.edsl.checkers.sqldynamic;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.string.samplegen.SampleGenerator;

public class DynamicSQLChecker implements IAbstractStringChecker {

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
		
		System.out.println("============================================");
		
		for (IStringNodeDescriptor desc: descriptors) {
			System.out.println("ABS: " + desc.getAbstractValue());
			List<String> concreteStr = SampleGenerator.getConcreteStrings(desc.getAbstractValue());
//			System.out.println(conc);
			totalConcrete += concreteStr.size();
			
			int duplicates = 0;
			for (String s: concreteStr) {
				Integer soFar = concretes.get(s);
				duplicates = 0;
				if (soFar == null) {
					System.out.println("CON: " + s);
					try {
						analyzer.validate(s);
					} catch (SQLException e) {
						System.out.println("    ERR: " + e.getMessage());
						errorHandler.handleSQLError(e.getMessage(), desc);
					}
					
					concretes.put(s, 1);
				}
				else {
					concretes.put(s, soFar+1);
					duplicates++;
				}
			}
			System.out.println("DUPLICATES: " + duplicates);
			System.out.println("____________________________________________");
		}
		
		System.out.println("TOTAL ABSTRACT COUNT: " + descriptors.size());
		System.out.println("TOTAL CONCRETE COUNT: " + totalConcrete);
		System.out.println("DIFFERENT CONCRETE COUNT: " + concretes.size());
	}

}
