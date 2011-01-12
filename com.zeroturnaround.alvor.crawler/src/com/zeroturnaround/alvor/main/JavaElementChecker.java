package com.zeroturnaround.alvor.main;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.ISQLErrorHandler;
import com.zeroturnaround.alvor.checkers.sqldynamic.DynamicSQLChecker;
import com.zeroturnaround.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Measurements;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.configuration.DataSourceProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.crawler.AbstractStringEvaluator;
import com.zeroturnaround.alvor.util.PositionUtil;

/**
 * This is main class
 * - finds hotspots
 * - creates abstract strings
 * - runs checkers 
 * 
 */
public class JavaElementChecker {

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
	public List<NodeDescriptor> findAndEvaluateHotspots(IJavaElement[] scope, ProjectConfiguration conf) {
		Measurements.resetAll();
		
		Timer timer = new Timer();
		timer.start("TIMER: string construction");
		List<HotspotPattern> requests = conf.getHotspots();
		if (requests.isEmpty()) {
			throw new IllegalArgumentException("No hotspot definitions found in options");
		}
		List<NodeDescriptor> result = AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(requests, scope, 0, null);
		timer.printTime(); // String construction
		
		LOG.message(Measurements.parseTimer);
		LOG.message(Measurements.methodDeclSearchTimer);
		LOG.message(Measurements.argumentSearchTimer);
		
		return result;
	}

	public void processHotspots(
		List<NodeDescriptor> hotspots, 
		ISQLErrorHandler errorHandler, 
		List<IAbstractStringChecker> checkers, 
		ProjectConfiguration configuration) {
		
//		Map<String, Integer> connMap = new Hashtable<String, Integer>();
		int unsupportedCount = 0;
		
		List<StringNodeDescriptor> validHotspots = new ArrayList<StringNodeDescriptor>();
		for (NodeDescriptor hotspot : hotspots) {
			if (hotspot instanceof StringNodeDescriptor) {
				validHotspots.add((StringNodeDescriptor) hotspot);
				// TODO this makes log quite big
				HOTSPOTS_LOG.message("STRING node desc, file=" + PositionUtil.getLineString(hotspot.getPosition())
						+ ", str=" + ((StringNodeDescriptor) hotspot).getAbstractValue());
				
				
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
		checkValidHotspots(validHotspots, errorHandler, checkers, configuration);
		
		LOG.message("Processed " + hotspots.size() + " node descriptors, "
				+ validHotspots.size() + " of them with valid abstract strings, "
				+ "unsupported cases: " + unsupportedCount);

		
		
//		LOG.message("CONNECTION DESCRIPTORS");
//		for (Map.Entry<String, Integer> entry : connMap.entrySet()) {
//			LOG.message("COUNT: " + entry.getValue() + ", EXP: " + entry.getKey());
//		}
	}

	private void checkValidHotspots(
			List<StringNodeDescriptor> hotspots, 
			ISQLErrorHandler errorHandler, 
			List<IAbstractStringChecker> checkers, 
			ProjectConfiguration configuration) {
		
		Timer timer = new Timer();
		timer.start("TIMER checking");
		
		if (!dynamicCheckerIsConfigured(configuration)) {
			errorHandler.handleSQLWarning(
					"SQL checker: Test-database is not configured, SQL testing is not performed",
					null);
		}
		
		try {
			if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.ALL_CHECKERS) {
				assert LOG.message("Checking with all checkers");
				checkValidHotspotsWithAllCheckers(hotspots, errorHandler, checkers, configuration);
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
				if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.PREFER_DYNAMIC) {
					assert LOG.message("Prefering dynamic checker");
					checkValidHotspotsPreferDynamic(hotspots, errorHandler, dynamicChecker, staticChecker, configuration);
				}
				else if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.PREFER_STATIC) { 
					assert LOG.message("Prefering static checker");
					checkValidHotspotsPreferStatic(hotspots, errorHandler, dynamicChecker, staticChecker, configuration);
				}
				else {
					throw new CheckerException("Unknown checking strategy", null);
				}
			}
		}
		catch (CheckerException e) {
			errorHandler.handleSQLError("SQL checker exception: " + e.getMessage(), e.getPosition());
		}
		
		timer.printTime();
	}
	
	private void checkValidHotspotsWithAllCheckers(
			List<StringNodeDescriptor> hotspots, 
			ISQLErrorHandler errorHandler, 
			List<IAbstractStringChecker> checkers, 
			ProjectConfiguration configuration) throws CheckerException {
		
		for (IAbstractStringChecker checker : checkers) {
			if (checker instanceof DynamicSQLChecker 
					&& !dynamicCheckerIsConfigured(configuration)) {
				errorHandler.handleSQLWarning("SQL checker: testing database is not configured", 
						null);
				
			} else {
				Timer timer = new Timer();
				timer.start("TIMER checker=" + checker.getClass().getName());
				checker.checkAbstractStrings(hotspots, errorHandler, configuration);
				timer.printTime();
			}
		}
	}
	
	private boolean dynamicCheckerIsConfigured(ProjectConfiguration configuration) {
		DataSourceProperties props = configuration.getDefaultDataSource();
		return props != null && props.getUrl() != null && !props.getUrl().trim().isEmpty()
			&& props.getDriverName() != null && !props.getDriverName().trim().isEmpty();
	}
	
	private void checkValidHotspotsPreferDynamic(
			List<StringNodeDescriptor> descriptors, 
			ISQLErrorHandler errorHandler, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			ProjectConfiguration configuration) {
		
		for (StringNodeDescriptor descriptor : descriptors) {
			try {
				if (dynamicCheckerIsConfigured(configuration)) { 
					// use staticChecker only when dynamic gives error
					boolean dynResult = false;
					try {
						dynResult = dynamicChecker.checkAbstractString(descriptor, errorHandler, configuration);
					} finally {
						if (!dynResult) {
							staticChecker.checkAbstractString(descriptor, errorHandler, configuration);
						}
					}
				} else {
					staticChecker.checkAbstractString(descriptor, errorHandler, configuration);
				}
			} catch (Exception e) {
				LOG.exception(e);
				errorHandler.handleSQLWarning("Error during checking: " + e.getMessage(), 
						descriptor.getPosition());
			}
		}
	}
	
	
	private void checkValidHotspotsPreferStatic(
			List<StringNodeDescriptor> descriptors, 
			ISQLErrorHandler errorHandler, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			ProjectConfiguration configuration) {
		
		for (StringNodeDescriptor descriptor : descriptors) {
			try {
				// use dynamic only when static didn't find anything wrong, or when it crashed
				// note that logic is different compared to PreferDynamic case
				boolean staticResult = true;
				try {
					staticResult = staticChecker.checkAbstractString(descriptor, errorHandler, configuration);
				} finally {
					if (staticResult && dynamicCheckerIsConfigured(configuration)) {
						dynamicChecker.checkAbstractString(descriptor, errorHandler, configuration);
					}
				}
			} catch (Exception e) {
				// should be able to proceed with next descriptors using static checker
				LOG.exception(e);
				errorHandler.handleSQLWarning("Error during checking: " + e.getMessage(), 
						descriptor.getPosition());
			}
		}
	}
	
}
