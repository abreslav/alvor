package ee.stacc.productivity.edsl.main;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import ee.stacc.productivity.edsl.crawler.AbstractStringEvaluator;
import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;
import ee.stacc.productivity.edsl.crawler.StringNodeDescriptor;

/**
 * This is main class
 * - finds "prepareStatement"-s
 * - creates abstract strings
 * - creates abstract SQL structures (containing either error msg or resultset metadata)
 * 
 */
public class SQLUsageChecker {
	
	public void checkProject(IJavaProject project, IAbstractStringChecker checker, ISQLErrorHandler errorHandler) {
		NodeSearchEngine.clearCache();
		List<StringNodeDescriptor> descriptors = AbstractStringEvaluator.evaluateMethodArgumentAtCallSites
			("java.sql.Connection", "prepareStatement", 1, project, 0);
	
		checker.checkAbstractStrings(descriptors, errorHandler);
	}
}
