package ee.stacc.productivity.edsl.main;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import ee.stacc.productivity.edsl.crawler.AbstractStringEvaluator;
import ee.stacc.productivity.edsl.crawler.NodeRequest;
import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;
import ee.stacc.productivity.edsl.crawler.StringNodeDescriptor;

/**
 * This is main class
 * - finds hotspots
 * - creates abstract strings
 * - runs checkers 
 * 
 */
public class SQLUsageChecker {
	
	public void checkJavaElement(List<StringNodeDescriptor> hotspots, ISQLErrorHandler errorHandler, IAbstractStringChecker... checkers) {
	
		for (StringNodeDescriptor stringNodeDescriptor : hotspots) {
			System.out.println(stringNodeDescriptor.getAbstractValue());
		}
		
		
		for (IAbstractStringChecker checker : checkers) {
			checker.checkAbstractStrings(hotspots, errorHandler);
		}
	}

	public List<StringNodeDescriptor> findHotspots(IJavaElement scope) {
		NodeSearchEngine.clearCache();
		List<StringNodeDescriptor> descriptors = AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(Arrays.asList(
				new NodeRequest("org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "queryForInt", 1)
				,
				new NodeRequest("org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "queryForObject", 1)
				,
				new NodeRequest("org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "queryForLong", 1)
				,
				new NodeRequest("org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "query", 1)
				,
				new NodeRequest("java.sql.Connection", "prepareStatement", 1)
				,
				new NodeRequest("org.araneaframework.backend.list.helper.ListSqlHelper", "setSqlQuery", 1)
			), scope, 0);
		return descriptors;
	}
}
