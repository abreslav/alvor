package com.zeroturnaround.alvor.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.cache.PositionUtil;
import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.INodeDescriptor;
import com.zeroturnaround.alvor.checkers.ISQLErrorHandler;
import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.checkers.sqldynamic.DynamicSQLChecker;
import com.zeroturnaround.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
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
	
	// NB! smartChecking==true can hide problems with static checker
	// so for testing it should be set false
	private boolean smartChecking = true;

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
		
		Timer timer = new Timer();
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
				UnsupportedNodeDescriptor und = (UnsupportedNodeDescriptor) hotspot; 
				assert LOG.message("UNSUPPORTED node desc, file=" + PositionUtil.getLineString(hotspot.getPosition())
						+ ", msg=" + (und).getProblemMessage());
				unsupportedCount++;
				
				String msg = "Unsupported SQL construction: " + und.getProblemMessage(); 
				if (und.getErrorPosition() != null && !und.getPosition().equals(und.getErrorPosition())) {
					msg += " at: " + PositionUtil.getLineString(und.getErrorPosition());
				}
				errorHandler.handleSQLWarning(msg, hotspot.getPosition());
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
		
		Timer timer = new Timer();
		timer.start("TIMER checking");
		
		try {
			if (this.smartChecking) {
				// TODO maybe it's better to get specific checkers more directly
				// (current scheme was made for checking everything with all checkers
				// and for dynamic addition of new checkers) 
				IAbstractStringChecker dynamicChecker = null;
				IAbstractStringChecker staticChecker = null;
				for (IAbstractStringChecker checker : checkers) {
					if (checker instanceof DynamicSQLChecker) {
						dynamicChecker = checker;
					}
					if (checker instanceof SyntacticalSQLChecker) {
						staticChecker = checker;
					}
				}
				
				assert (dynamicChecker != null && staticChecker != null);
				checkValidHotspotsSmartly(hotspots, errorHandler, dynamicChecker, staticChecker, options);
			}
			else {
				checkValidHotspotsFully(hotspots, errorHandler, checkers, options);
			}
		}
		catch (CheckerException e) {
			errorHandler.handleSQLError("SQL checker exception: " + e.getMessage(), e.getPosition());
		}
		
		timer.printTime();
	}
	
	private void checkValidHotspotsFully(
			List<IStringNodeDescriptor> hotspots, 
			ISQLErrorHandler errorHandler, 
			List<IAbstractStringChecker> checkers, 
			Map<String, String> options) throws CheckerException {
		
		for (IAbstractStringChecker checker : checkers) {
			Timer timer = new Timer();
			timer.start("TIMER checker=" + checker.getClass().getName());
			checker.checkAbstractStrings(hotspots, errorHandler, options);
			timer.printTime();
		}
	}
	
	private void checkValidHotspotsSmartly(
			List<IStringNodeDescriptor> descriptors, 
			ISQLErrorHandler errorHandler, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			Map<String, String> options) throws CheckerException {
		
		boolean dynamicIsConfigured = options.get("DBUrl") != null; 
		
		for (IStringNodeDescriptor descriptor : descriptors) {
			if (dynamicIsConfigured) { 
				// use staticChecker only when dynamic gives error
				if (!dynamicChecker.checkAbstractString(descriptor, errorHandler, options)) {
					staticChecker.checkAbstractString(descriptor, errorHandler, options);
				}
			} else {
				staticChecker.checkAbstractString(descriptor, errorHandler, options);
			}
		}
	}
	
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
	
	public void setSmartChecking(boolean smartChecking) {
		this.smartChecking = smartChecking;
	}
	
}
