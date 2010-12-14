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
import com.zeroturnaround.alvor.string.Position;

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
	private static final ILog HOTSPOTS_LOG = Logs.getLog("Hotspots");
	
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
		List<INodeDescriptor> result = AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(requests, scope, 0, null);
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
				// TODO this makes log quite big
				HOTSPOTS_LOG.message("STRING node desc, file=" + PositionUtil.getLineString(hotspot.getPosition())
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
		
		if (!dynamicCheckerIsConfigured(options)) {
			errorHandler.handleSQLWarning(
					"SQL checker: Test-database is not configured, SQL testing is not performed",
					new Position(options.get("SourceFileName"), 0, 0));
		}
		
		try {
			String strategy = options.get("checkingStrategy");
			if (strategy == null || strategy.isEmpty()) {
				strategy = "preferStatic";
			}
			if (strategy.equals("allCheckers")) {
				assert LOG.message("Checking with all checkers");
				checkValidHotspotsWithAllCheckers(hotspots, errorHandler, checkers, options);
			}
			
			else {
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
				if (strategy.equals("preferDynamic")) {
					assert LOG.message("Prefering dynamic checker");
					checkValidHotspotsPreferDynamic(hotspots, errorHandler, dynamicChecker, staticChecker, options);
				}
				else if (strategy.equals("preferStatic")) { 
					assert LOG.message("Prefering static checker");
					checkValidHotspotsPreferStatic(hotspots, errorHandler, dynamicChecker, staticChecker, options);
				}
				else {
					throw new CheckerException("Unknown checking strategy",
							new Position(options.get("SourceFileName"), 0, 0));
				}
			}
		}
		catch (CheckerException e) {
			errorHandler.handleSQLError("SQL checker exception: " + e.getMessage(), e.getPosition());
		}
		
		timer.printTime();
	}
	
	private void checkValidHotspotsWithAllCheckers(
			List<IStringNodeDescriptor> hotspots, 
			ISQLErrorHandler errorHandler, 
			List<IAbstractStringChecker> checkers, 
			Map<String, String> options) throws CheckerException {
		
		for (IAbstractStringChecker checker : checkers) {
			if (checker instanceof DynamicSQLChecker 
					&& !dynamicCheckerIsConfigured(options)) {
				errorHandler.handleSQLWarning("SQL checker: testing database is not configured", 
						new Position(options.get("SourceFileName"), 0, 0));
				
			} else {
				Timer timer = new Timer();
				timer.start("TIMER checker=" + checker.getClass().getName());
				checker.checkAbstractStrings(hotspots, errorHandler, options);
				timer.printTime();
			}
		}
	}
	
	private boolean dynamicCheckerIsConfigured(Map<String, String> options) {
		return options.get("DBUrl") != null && !options.get("DBUrl").trim().isEmpty()
			&& options.get("DBDriverName") != null && !options.get("DBDriverName").trim().isEmpty();
	}
	
	private void checkValidHotspotsPreferDynamic(
			List<IStringNodeDescriptor> descriptors, 
			ISQLErrorHandler errorHandler, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			Map<String, String> options) throws CheckerException {
		
		for (IStringNodeDescriptor descriptor : descriptors) {
			if (dynamicCheckerIsConfigured(options)) { 
				// use staticChecker only when dynamic gives error
				boolean dynResult = false;
				try {
					dynResult = dynamicChecker.checkAbstractString(descriptor, errorHandler, options);
				} finally {
					if (!dynResult) {
						staticChecker.checkAbstractString(descriptor, errorHandler, options);
					}
				}
			} else {
				staticChecker.checkAbstractString(descriptor, errorHandler, options);
			}
		}
	}
	
	
	private void checkValidHotspotsPreferStatic(
			List<IStringNodeDescriptor> descriptors, 
			ISQLErrorHandler errorHandler, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			Map<String, String> options) throws CheckerException {
		
		for (IStringNodeDescriptor descriptor : descriptors) {
			// use dynamic only when static didn't find anything wrong, or when it crashed
			// note that logic is different compared to PreferDynamic case
			boolean staticResult = true;
			try {
				staticResult = staticChecker.checkAbstractString(descriptor, errorHandler, options);
			} finally {
				if (staticResult && dynamicCheckerIsConfigured(options)) {
					dynamicChecker.checkAbstractString(descriptor, errorHandler, options);
				}
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
}
