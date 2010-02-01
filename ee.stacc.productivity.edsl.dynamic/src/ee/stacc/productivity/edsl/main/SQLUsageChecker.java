package ee.stacc.productivity.edsl.main;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import ee.stacc.productivity.edsl.crawler.AbstractStringEvaluator;
import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;
import ee.stacc.productivity.edsl.crawler.StringNodeDescriptor;
import ee.stacc.productivity.edsl.db.SQLStringAnalyzer;
import ee.stacc.productivity.edsl.samplegen.SampleGenerator;

/**
 * This is main class
 * - finds "prepareStatement"-s
 * - creates abstract strings
 * - creates abstract SQL structures (containing either error msg or resultset metadata)
 * 
 */
public class SQLUsageChecker {
	private SQLStringAnalyzer analyzer = new SQLStringAnalyzer();
	
	public void checkProject(IJavaProject project, ISQLErrorHandler errorHandler) {
		NodeSearchEngine.clearCache();
		List<StringNodeDescriptor> descriptors = AbstractStringEvaluator.evaluateMethodArgumentAtCallSites
			("java.sql.Connection", "prepareStatement", 1, project, 0);
	
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
						errorHandler.handleSQLError(e, desc.getFile(), 
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
