package ee.stacc.productivity.edsl.main;

import java.util.ArrayList;
import java.util.Collections;
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

	private static final String HOTSPOTS = "hotspots";

	/*
	 * The map must contain an entry 
	 * hotspots=classname,methodname,index;classname,methodname,index;...
	 * E.g.:
	 * hotspots=java.util.Connection,prepareStatement,1;blah.blah.Blah,blah,5
	 * Trailing ';' is not required 
	 */
	public List<IStringNodeDescriptor> findHotspots(IJavaElement scope, Map<String, Object> options) {
		List<NodeRequest> requests = parseNodeRequests(options);
		if (requests.isEmpty()) {
			throw new IllegalArgumentException("No hotspots found");
		}
		NodeSearchEngine.clearCache();
		return AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(requests, scope, 0);
	}

	public void checkHotspots(
			List<IStringNodeDescriptor> hotspots, 
			ISQLErrorHandler errorHandler, 
			List<IAbstractStringChecker> checkers, 
			Map<String, Object> options) {
	
		for (IAbstractStringChecker checker : checkers) {
			checker.checkAbstractStrings(hotspots, errorHandler, options);
		}
	}

	private List<NodeRequest> parseNodeRequests(Map<String, Object> options) {
		if (options == null) {
			return Collections.emptyList();
		}
		Object option = options.get(HOTSPOTS);
		if (option == null) {
			return Collections.emptyList();
		}
		String allHotspots = option.toString();
		
		System.out.println("Hotspots:");
		List<NodeRequest> requests = new ArrayList<NodeRequest>();
		for (String hotspot : allHotspots.split(";")) {
			if (hotspot.length() == 0) {
				continue;
			}
			String[] split = hotspot.split(",");
			if (split.length != 3) {
				System.err.println("Malformed hotspot: " + hotspot);
				continue;
			}
			String className = split[0];
			String methodName = split[1];
			String argumentIndex = split[2];
			try {
				int index = Integer.parseInt(argumentIndex);
				NodeRequest nodeRequest = new NodeRequest(className, methodName, index);
				requests.add(nodeRequest);
			} catch (NumberFormatException e) {
				System.err.println("Number format error: " + argumentIndex);
			}
		}
		return requests;
	}
	
}
