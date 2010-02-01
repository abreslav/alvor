package ee.stacc.productivity.edsl.main;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Expression;

import ee.stacc.productivity.edsl.crawler.Crawler;
import ee.stacc.productivity.edsl.db.SQLStringAnalyzer;
import ee.stacc.productivity.edsl.samplegen.SampleGenerator;
import ee.stacc.productivity.edsl.string.IAbstractString;

/**
 * This is main class in a sense.
 * - finds "prepareStatement"-s
 * - creates abstract strings
 * - creates abstract SQL structures (containing either error msg or resultset metadata)
 * 
 */
public class SQLUsageChecker {
	private SQLStringAnalyzer analyzer = new SQLStringAnalyzer();
	
	
	public void checkProject(IJavaProject project) {
		checkElement(project);
	}
	
	public void checkElement(IJavaElement scope) { // scope can be eg. file or project
		Crawler.clearCache();
		List<IAbstractString> aStrings = Crawler.findArgumentAbstractValuesAtCallSites
			("java.sql.Connection", "prepareStatement", 1, scope, 1);
	
		int totalConcrete = 0;
		Hashtable<String, Integer> concretes = new Hashtable<String, Integer>();
		
		System.out.println("============================================");
		for (IAbstractString aStr: aStrings) {
			List<String> conc = SampleGenerator.getConcreteStrings(aStr);
			System.out.println(conc.size() + ":" + aStr);
			totalConcrete += conc.size();
			
			for (String s: conc) {
				Integer soFar = concretes.get(s);
				if (soFar == null) {
					try {
						analyzer.validate(s);
					} catch (SQLException e) {
						
					}
					concretes.put(s, 1);
				}
				else {
					concretes.put(s, (soFar == null) ? 1 : soFar+1);
				}
			}
			//checkParseSQLNode(node);
		}
		
		System.out.println("TOTAL ABSTRACT COUNT: " + aStrings.size());
		System.out.println("TOTAL CONCRETE COUNT: " + totalConcrete);
		System.out.println("DIFFERENT CONCRETE COUNT: " + concretes.size());
	}
	
	/*
	private void checkParseSQLNode(Expression node) {
		try {
			IAbstractString aStr = OldAbstractStringEvaluator.getValOf(node, 1);
			AbstractSQLStructure struct = new AbstractSQLStructure(aStr, analyzer);
			
			if (struct.getErrorMsg() != null) {
				// TODO if there is wrapper around parseStatement
				// then there may be better places for error marker
				markSQLError(node, aStr, struct.getErrorMsg());
			}
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	*/
	
	public void markSQLError(Expression node, IAbstractString aStr, String message) {
		//System.err.println(struct.getErrorMsg());
		
		System.err.println("MARK_ERROR: node: " + node.toString());
		System.err.println("MARK_ERROR: message: " + message);
		//System.err.println("MARK_ERROR: file: " + AbstractStringEvaluator.getNodeFile(node).toString());
		System.err.println("MARK_ERROR: start: " + node.getStartPosition());
		
		
		
		//createMarker(message, ERROR_MARKER_ID, AbstractStringEvaluator.getNodeFile(node),
		//		node.getStartPosition(), node.getStartPosition()+node.getLength());
		
		
		/*
		List<AbstractString> aStrList = aStr.getExpandedStrings();
		
		for (AbstractString tmpAStr: aStrList) {
			createMarker(message, ERROR_MARKER_ID, tmpAStr.getFile(),
					tmpAStr.getStartPos(), tmpAStr.getEndPos());
		}
		*/
	}
	
}
