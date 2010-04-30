package ee.stacc.productivity.edsl.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.conntracker.ConnectionDescriptor;
import ee.stacc.productivity.edsl.conntracker.ConnectionTracker;
import ee.stacc.productivity.edsl.crawler.AbstractStringEvaluator;
import ee.stacc.productivity.edsl.crawler.StringNodeDescriptor;
import ee.stacc.productivity.edsl.crawler.NewASE;
import ee.stacc.productivity.edsl.crawler.NodeRequest;
import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;
import ee.stacc.productivity.edsl.crawler.UnsupportedNodeDescriptor;
import ee.stacc.productivity.edsl.string.IPosition;

/**
 * This is main class
 * - finds hotspots
 * - creates abstract strings
 * - runs checkers 
 * 
 */
public class JavaElementChecker {

	private static final String HOTSPOTS = "hotspots";
	private static final ILog LOG = Logs.getLog(JavaElementChecker.class);

	/*
	 * The map must contain an entry 
	 * hotspots=classname,methodname,index;classname,methodname,index;...
	 * E.g.:
	 * hotspots=java.util.Connection,prepareStatement,1;blah.blah.Blah,blah,5
	 * Trailing ';' is not required 
	 * 
	 * Actually returns abstract strings corresponding to hotspots
	 * (or markers for unsupported cases)  
	 * TODO rename?
	 */
	public List<INodeDescriptor> findHotspots(IJavaElement[] scope, Map<String, Object> options) {
		List<NodeRequest> requests = parseNodeRequests(options);
		if (requests.isEmpty()) {
			throw new IllegalArgumentException("No hotspots found");
		}
		NodeSearchEngine.clearCache();
		return AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(requests, scope, 0);
//		return NewASE.evaluateMethodArgumentAtCallSites(requests, scope, 0);
	}

	public void processHotspots(
		List<INodeDescriptor> hotspots, 
		ISQLErrorHandler errorHandler, 
		List<IAbstractStringChecker> checkers, 
		Map<String, Object> options) {
		
		Map<String, Integer> connMap = new Hashtable<String, Integer>();
		
		List<IStringNodeDescriptor> validHotspots = new ArrayList<IStringNodeDescriptor>();
		for (INodeDescriptor hotspot : hotspots) {
			if (hotspot instanceof IStringNodeDescriptor) {
				validHotspots.add((IStringNodeDescriptor) hotspot);
				
				// collect connection info
				ConnectionDescriptor connDesc = 
					ConnectionTracker.getConnectionDescriptorForHotspot(hotspot.getPosition());
				String exp = connDesc.getExpression();
				Integer prevCount = connMap.get(exp);
				connMap.put(exp, prevCount == null ? 1 : prevCount + 1);
			}
			else if (hotspot instanceof UnsupportedNodeDescriptor) {
				errorHandler.handleSQLWarning(((UnsupportedNodeDescriptor)hotspot).getProblemMessage(),
						hotspot.getPosition());
			}
		}
		checkValidHotspots(validHotspots, errorHandler, checkers, options);
		
		
		LOG.message("CONNECTION DESCRIPTORSsss");
		for (Map.Entry<String, Integer> entry : connMap.entrySet()) {
			LOG.message("COUNT: " + entry.getValue() + ", EXP: " + entry.getKey());
		}
	}

	private void checkValidHotspots(
			List<IStringNodeDescriptor> hotspots, 
			ISQLErrorHandler errorHandler, 
			List<IAbstractStringChecker> checkers, 
			Map<String, Object> options) {
	
		LOG.message("Abstract strings:");

		for (INodeDescriptor descriptor : hotspots) {
			if (descriptor instanceof IStringNodeDescriptor) {
				LOG.message(((IStringNodeDescriptor)descriptor).getAbstractValue());
			}
		}
		
		for (IAbstractStringChecker checker : checkers) {
			checker.checkAbstractStrings(hotspots, errorHandler, options);
		}
	}
	
	public void recheckHotspot(IPosition position, ISQLErrorHandler errorHandler, 
			List<IAbstractStringChecker> checkers, 
			Map<String, Object> options) {
//		AbstractStringEvaluator.evaluateExpression(null)
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
		
		LOG.message("Hotspots:");
		List<NodeRequest> requests = new ArrayList<NodeRequest>();
		for (String hotspot : allHotspots.split(";")) {
			if (hotspot.length() == 0) {
				continue;
			}
			String[] split = hotspot.split(",");
			if (split.length != 3) {
				LOG.message("Malformed hotspot: " + hotspot);
				continue;
			}
			String className = split[0];
			String methodName = split[1];
			String argumentIndex = split[2];
			try {
				int index = Integer.parseInt(argumentIndex);
				NodeRequest nodeRequest = new NodeRequest(className, methodName, index);
				requests.add(nodeRequest);
				LOG.message(nodeRequest);
			} catch (NumberFormatException e) {
				LOG.message("Number format error: " + argumentIndex);
			}
		}
		return requests;
	}
	
}
