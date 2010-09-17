package com.zeroturnaround.alvor.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.cache.PositionUtil;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.INodeDescriptor;
import com.zeroturnaround.alvor.checkers.ISQLErrorHandler;
import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Measurements;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.crawler.AbstractStringEvaluator;
import com.zeroturnaround.alvor.crawler.NodeRequest;
import com.zeroturnaround.alvor.crawler.UnsupportedNodeDescriptor;

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
	private Timer timer = new Timer();

	/*
	 * The map must contain an entry 
	 * hotspots=className,methodName,index;className,methodName,index;...
	 * E.g.:
	 * hotspots=java.util.Connection,prepareStatement,1;blah.blah.Blah,blah,5
	 * Trailing ';' is not required 
	 * 
	 * Actually returns abstract strings corresponding to hotspots
	 * (or markers for unsupported cases)  
	 * TODO rename?
	 */
	public List<INodeDescriptor> findAndEvaluateHotspots(IJavaElement[] scope, Map<String, String> options) {
		Measurements.resetAll();
		timer.start("TIMER: string construction");
		List<NodeRequest> requests = parseNodeRequests(options);
		if (requests.isEmpty()) {
			throw new IllegalArgumentException("No hotspot definitions found in options");
		}
		List<INodeDescriptor> result = AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(requests, scope, 0);
		timer.printTime(); // String construction
		
		LOG.message(Measurements.parseTimer);
		LOG.message(Measurements.methodDeclSearchTimer);
		LOG.message(Measurements.argumentSearchTimer);
		
		return result;
	}

	public void processHotspots(
		List<INodeDescriptor> hotspots, 
		ISQLErrorHandler errorHandler, 
		List<IAbstractStringChecker> checkers, 
		Map<String, String> options) {
		
//		Map<String, Integer> connMap = new Hashtable<String, Integer>();
		int unsupportedCount = 0;
		
		List<IStringNodeDescriptor> validHotspots = new ArrayList<IStringNodeDescriptor>();
		for (INodeDescriptor hotspot : hotspots) {
			if (hotspot instanceof IStringNodeDescriptor) {
				validHotspots.add((IStringNodeDescriptor) hotspot);
				assert LOG.message("STRING node desc, file=" + PositionUtil.getLineString(hotspot.getPosition())
						+ ", str=" + ((IStringNodeDescriptor) hotspot).getAbstractValue());
				
				
//				// collect connection info
//				ConnectionDescriptor connDesc = 
//					ConnectionTracker.getConnectionDescriptorForHotspot(hotspot.getPosition());
//				
//				String exp = connDesc.getExpression();
//				Integer prevCount = connMap.get(exp);
//				connMap.put(exp, prevCount == null ? 1 : prevCount + 1);
			}
			else if (hotspot instanceof UnsupportedNodeDescriptor) {
				assert LOG.message("UNSUPPORTED node desc, file=" + PositionUtil.getLineString(hotspot.getPosition())
						+ ", msg=" + ((UnsupportedNodeDescriptor) hotspot).getProblemMessage());
				unsupportedCount++;
				errorHandler.handleSQLWarning(
						"Unsupported SQL construction: " + ((UnsupportedNodeDescriptor)hotspot).getProblemMessage(),
						hotspot.getPosition());
			}
			else {
				throw new IllegalArgumentException("Unknown type of INodeTypeDescriptor: " + hotspot.getClass().getName());
			}
		}
		checkValidHotspots(validHotspots, errorHandler, checkers, options);
		
		LOG.message("Processed " + hotspots.size() + " node descriptors, "
				+ validHotspots.size() + " of them with valid abstract strings, "
				+ "unsupported cases: " + unsupportedCount);

		
		
//		LOG.message("CONNECTION DESCRIPTORS");
//		for (Map.Entry<String, Integer> entry : connMap.entrySet()) {
//			LOG.message("COUNT: " + entry.getValue() + ", EXP: " + entry.getKey());
//		}
	}

	private void checkValidHotspots(
			List<IStringNodeDescriptor> hotspots, 
			ISQLErrorHandler errorHandler, 
			List<IAbstractStringChecker> checkers, 
			Map<String, String> options) {
	
//		assert LOG.message("Abstract strings:");
//
//		for (INodeDescriptor descriptor : hotspots) {
//			if (descriptor instanceof IStringNodeDescriptor) {
//				assert LOG.message(((IStringNodeDescriptor)descriptor).getAbstractValue());
//			}
//		}
		
		for (IAbstractStringChecker checker : checkers) {
			timer.start("TIMER checker=" + checker.getClass().getName());
			checker.checkAbstractStrings(hotspots, errorHandler, options);
			timer.printTime();
		}
	}
	
//	public void recheckHotspot(IPosition position, ISQLErrorHandler errorHandler, 
//			List<IAbstractStringChecker> checkers, 
//			Map<String, Object> options) {
////		AbstractStringEvaluator.evaluateExpression(null)
//	}
	
	private List<NodeRequest> parseNodeRequests(Map<String, String> options) {
		if (options == null) {
			return Collections.emptyList();
		}
		Object option = options.get(HOTSPOTS);
		if (option == null) {
			return Collections.emptyList();
		}
		String allHotspots = option.toString();
		
		assert LOG.message("Hotspots:");
		List<NodeRequest> requests = new ArrayList<NodeRequest>();
		for (String hotspot : allHotspots.split(";")) {
			if (hotspot.length() == 0) {
				continue;
			}
			String[] split = hotspot.split(",");
			if (split.length != 3) {
				assert LOG.message("Malformed hotspot: " + hotspot);
				continue;
			}
			String className = split[0];
			String methodName = split[1];
			String argumentIndex = split[2];
			try {
				int index = Integer.parseInt(argumentIndex);
				NodeRequest nodeRequest = new NodeRequest(className, methodName, index);
				requests.add(nodeRequest);
				assert LOG.message(nodeRequest);
			} catch (NumberFormatException e) {
				assert LOG.message("Number format error: " + argumentIndex);
			}
		}
		return requests;
	}
	
}
