package ee.stacc.productivity.edsl.main;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Expression;

import ee.stacc.productivity.edsl.crawler.AbstractStringEvaluator;
import ee.stacc.productivity.edsl.crawler.Crawler;
import ee.stacc.productivity.edsl.db.AbstractSQLStructure;
import ee.stacc.productivity.edsl.db.SQLStringAnalyzer;
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
		List<IAbstractString> aStrings = Crawler.findArgumentAbstractValuesAtCallSites
			("java.sql.Connection", "prepareStatement", 1, scope, 1);
	
		
		System.out.println("============================================");
		for (IAbstractString aStr: aStrings) {
			System.out.println(aStr);
			//checkParseSQLNode(node);
		}
		
		System.out.println("TOTAL COUNT: " + aStrings.size());
	}
	
	
	private void checkParseSQLNode(Expression node) {
		try {
			IAbstractString aStr = AbstractStringEvaluator.getValOf(node, 1);
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
	
	public void markSQLError(Expression node, IAbstractString aStr, String message) {
		// TODO abs-stringi loomisel jäta meelde, kuhu näidata punane kriips
		// SQL-i komponendid näidata ära ainult siis, kui kõik on ühes meetodis koos
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
