package ee.stacc.productivity.edsl.main;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

import ee.stacc.productivity.edsl.crawler.StringNodeDescriptor;
import ee.stacc.productivity.edsl.db.SQLStringAnalyzer;
import ee.stacc.productivity.edsl.samplegen.SampleGenerator;

public class DynamicSQLChecker implements IAbstractStringChecker {

	public static final DynamicSQLChecker INSTANCE = new DynamicSQLChecker();
	
	private SQLStringAnalyzer analyzer = new SQLStringAnalyzer();

	private DynamicSQLChecker() {}
	
	@Override
	public void checkAbstractStrings(List<StringNodeDescriptor> descriptors,
			ISQLErrorHandler errorHandler) {
		int totalConcrete = 0;
		Hashtable<String, Integer> concretes = new Hashtable<String, Integer>();
		
		System.out.println("============================================");
		
		for (StringNodeDescriptor desc: descriptors) {
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
						errorHandler.handleSQLError(e.getMessage(), desc.getFile(), 
								desc.getCharStart(), desc.getCharLength());
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
