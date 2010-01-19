package ee.stacc.productivity.edsl.main;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ee.stacc.productivity.edsl.crawler.AbstractStringEvaluator;
import ee.stacc.productivity.edsl.crawler.ArgumentFinder;
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
	public static final String ERROR_MARKER_ID = "EclipseSQLPlugin.sqlerror";
	public static final String WARNING_MARKER_ID = "EclipseSQLPlugin.sqlwarning";
	ArgumentFinder argumentFinder = new ArgumentFinder();
	private SQLStringAnalyzer analyzer = new SQLStringAnalyzer();
	
	public void checkProject(IJavaProject project) {
		checkElement(project);
	}
	
	public void checkElement(IJavaElement scope) { // scope can be eg. file or project
		//cleanMarkers(scope);
		
		List<IAbstractString> aStrings = argumentFinder.findArgumentAbstractValues
			("prepareStatement", 1, scope);
	
		for (IAbstractString aStr: aStrings) {
			//System.out.println("ASTR: " + aStr);
			//checkParseSQLNode(node);
		}
		
		System.out.println("TOTAL COUNT: " + aStrings.size());
	}
	
	
	private void cleanMarkers(IJavaElement scope) {
		try {
			scope.getResource().deleteMarkers(ERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
			scope.getResource().deleteMarkers(WARNING_MARKER_ID, true, IResource.DEPTH_INFINITE);
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	private void checkParseSQLNode(Expression node) {
		try {
			IAbstractString aStr = AbstractStringEvaluator.getValOf(node);
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
		System.err.println("MARK_ERROR: file: " + AbstractStringEvaluator.getNodeFile(node).toString());
		System.err.println("MARK_ERROR: start: " + node.getStartPosition());
		
		createMarker(message, ERROR_MARKER_ID, AbstractStringEvaluator.getNodeFile(node),
				node.getStartPosition(), node.getStartPosition()+node.getLength());
		
		/*
		List<AbstractString> aStrList = aStr.getExpandedStrings();
		
		for (AbstractString tmpAStr: aStrList) {
			createMarker(message, ERROR_MARKER_ID, tmpAStr.getFile(),
					tmpAStr.getStartPos(), tmpAStr.getEndPos());
		}
		*/
	}
	
	void createMarker(String message, String markerType, IFile file, int charStart, int charEnd) {
	
		@SuppressWarnings("unchecked")
		HashMap<String, Comparable> map = new HashMap<String, Comparable>();
		MarkerUtilities.setMessage(map, message);
		map.put(IMarker.LOCATION, file.getFullPath().toString());
		map.put(IMarker.CHAR_START, charStart);
		map.put(IMarker.CHAR_END, charEnd);
	
		
		int severity = markerType.equals(WARNING_MARKER_ID) ? 
				IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;
		map.put(IMarker.SEVERITY, new Integer(severity));
		
		try {
			MarkerUtilities.createMarker(file, map, markerType);
		} catch (Exception e) {
			System.err.println("Error creating marker: " + e.getMessage());
		}
	}
	
}
