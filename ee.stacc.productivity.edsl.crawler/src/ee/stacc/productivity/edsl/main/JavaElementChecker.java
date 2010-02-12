package ee.stacc.productivity.edsl.main;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.crawler.AbstractStringEvaluator;
import ee.stacc.productivity.edsl.crawler.NodeRequest;
import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;

/**
 * This is main class
 * - finds hotspots
 * - creates abstract strings
 * - runs checkers 
 * 
 */
public class JavaElementChecker {

	public List<IStringNodeDescriptor> findHotspots(IJavaElement scope, Map<String, Object> options) {
		NodeSearchEngine.clearCache();
		List<IStringNodeDescriptor> descriptors = AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(Arrays.asList(
				new NodeRequest("org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "queryForInt", 1),
				new NodeRequest("org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "queryForObject", 1),
				new NodeRequest("org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "queryForLong", 1),
				new NodeRequest("org.springframework.jdbc.core.simple.SimpleJdbcTemplate", "query", 1),
				new NodeRequest("org.araneaframework.backend.list.helper.ListSqlHelper", "setSqlQuery", 1),
				new NodeRequest("java.sql.Connection", "prepareStatement", 1)
			), scope, 0);
		return descriptors;
	}
	
	public void checkHotspots(List<IStringNodeDescriptor> hotspots, ISQLErrorHandler errorHandler, List<IAbstractStringChecker> checkers, Map<String, Object> options) {
	
		for (IStringNodeDescriptor stringNodeDescriptor : hotspots) {
			System.out.println(stringNodeDescriptor.getAbstractValue());
		}
		
		
		for (IAbstractStringChecker checker : checkers) {
			checker.checkAbstractStrings(hotspots, errorHandler, options);
		}
	}
}
