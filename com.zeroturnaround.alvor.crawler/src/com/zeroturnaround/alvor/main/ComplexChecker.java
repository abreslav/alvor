package com.zeroturnaround.alvor.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.zeroturnaround.alvor.checkers.AbstractStringCheckerManager;
import com.zeroturnaround.alvor.checkers.AbstractStringCheckingResult;
import com.zeroturnaround.alvor.checkers.AbstractStringError;
import com.zeroturnaround.alvor.checkers.AbstractStringWarning;
import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.sqldynamic.DynamicSQLChecker;
import com.zeroturnaround.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.configuration.DataSourceProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.util.PositionUtil;

/**
 * This is main class
 * - finds hotspots
 * - creates abstract strings
 * - runs checkers 
 * 
 */
public class ComplexChecker {

	private static final ILog LOG = Logs.getLog(ComplexChecker.class);
	private static final ILog HOTSPOTS_LOG = Logs.getLog("Hotspots");
	

	public Collection<AbstractStringCheckingResult> checkNodeDescriptors(List<NodeDescriptor> hotspots, 
		ProjectConfiguration configuration) {
		
//		Map<String, Integer> connMap = new Hashtable<String, Integer>();
		int unsupportedCount = 0;
		
		Collection<AbstractStringCheckingResult> results = new ArrayList<AbstractStringCheckingResult>();
		List<IAbstractStringChecker> checkers = AbstractStringCheckerManager.INSTANCE.getCheckers();
		
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
				results.add(new AbstractStringWarning(msg, hotspot.getPosition()));
			}
			else {
				throw new IllegalArgumentException("Unknown type of INodeTypeDescriptor: " + hotspot.getClass().getName());
			}
		}
		results.addAll(checkStringNodeDescriptors(validHotspots, checkers, configuration));
		
		LOG.message("Processed " + hotspots.size() + " node descriptors, "
				+ validHotspots.size() + " of them with valid abstract strings, "
				+ "unsupported cases: " + unsupportedCount);

		
		
//		LOG.message("CONNECTION DESCRIPTORS");
//		for (Map.Entry<String, Integer> entry : connMap.entrySet()) {
//			LOG.message("COUNT: " + entry.getValue() + ", EXP: " + entry.getKey());
//		}
		
		return results;
	}

	private Collection<AbstractStringCheckingResult> checkStringNodeDescriptors(
			List<StringNodeDescriptor> hotspots, 
			List<IAbstractStringChecker> checkers, 
			ProjectConfiguration configuration) {
		
		Timer timer = new Timer();
		timer.start("TIMER checking");
		
		Collection<AbstractStringCheckingResult> results = new ArrayList<AbstractStringCheckingResult>();
		
		if (!dynamicCheckerIsConfigured(configuration)) {
			results.add(new AbstractStringWarning("SQL checker: Test-database is not configured, SQL testing is not performed",
					null));
		}
		
		try {
			if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.ALL_CHECKERS) {
				assert LOG.message("Checking with all checkers");
				results.addAll(checkStringNodeDescriptorsWithAllCheckers(hotspots,checkers, configuration));
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
					results.addAll(checkStringNodeDescriptorsPreferDynamic(hotspots, dynamicChecker, staticChecker, configuration));
				}
				else if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.PREFER_STATIC) { 
					assert LOG.message("Prefering static checker");
					results.addAll(checkStringNodeDescriptorsPreferStatic(hotspots, dynamicChecker, staticChecker, configuration));
				}
				else {
					throw new CheckerException("Unknown checking strategy", null);
				}
			}
		}
		catch (CheckerException e) {
			results.add(new AbstractStringError("SQL checker exception: " + e.getMessage(), e.getPosition()));
		}
		
		timer.printTime();
		return results;
	}
	
	private Collection<AbstractStringCheckingResult> checkStringNodeDescriptorsWithAllCheckers(
			List<StringNodeDescriptor> hotspots, 
			List<IAbstractStringChecker> checkers, 
			ProjectConfiguration configuration) throws CheckerException {
		
		Collection<AbstractStringCheckingResult> results = new ArrayList<AbstractStringCheckingResult>();
		for (IAbstractStringChecker checker : checkers) {
			if (checker instanceof DynamicSQLChecker 
					&& !dynamicCheckerIsConfigured(configuration)) {
				results.add(new AbstractStringWarning("SQL checker: testing database is not configured", null));
				
			} else {
				Timer timer = new Timer();
				timer.start("TIMER checker=" + checker.getClass().getName());
				results.addAll(checker.checkAbstractStrings(hotspots, configuration));
				timer.printTime();
			}
		}
		return results;
	}
	
	private boolean dynamicCheckerIsConfigured(ProjectConfiguration configuration) {
		DataSourceProperties props = configuration.getDefaultDataSource();
		return props != null && props.getUrl() != null && !props.getUrl().trim().isEmpty()
			&& props.getDriverName() != null && !props.getDriverName().trim().isEmpty();
	}
	
	private Collection<AbstractStringCheckingResult> checkStringNodeDescriptorsPreferDynamic(
			List<StringNodeDescriptor> descriptors, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			ProjectConfiguration configuration) {
		
		Collection<AbstractStringCheckingResult> results = new ArrayList<AbstractStringCheckingResult>();
		for (StringNodeDescriptor descriptor : descriptors) {
			Collection<AbstractStringCheckingResult> nodeResults = new ArrayList<AbstractStringCheckingResult>();
			try {
				if (dynamicCheckerIsConfigured(configuration)) {
					// use staticChecker only when dynamic gives error
					try {
						nodeResults.addAll(dynamicChecker.checkAbstractString(descriptor, configuration));
					} finally {
						if (!nodeResults.isEmpty()) {
							nodeResults.addAll(staticChecker.checkAbstractString(descriptor, configuration));
						}
					}
				} else {
					nodeResults.addAll(staticChecker.checkAbstractString(descriptor, configuration));
				}
			} catch (Exception e) {
				LOG.exception(e);
				nodeResults.add(new AbstractStringWarning("Error during checking: " + e.getMessage(), 
						descriptor.getPosition()));
			}
			results.addAll(nodeResults);
		}
		return results;
	}
	
	
	private Collection<AbstractStringCheckingResult> checkStringNodeDescriptorsPreferStatic(
			List<StringNodeDescriptor> descriptors, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			ProjectConfiguration configuration) {
		
		Collection<AbstractStringCheckingResult> results = new ArrayList<AbstractStringCheckingResult>();
		for (StringNodeDescriptor descriptor : descriptors) {
			Collection<AbstractStringCheckingResult> nodeResults = new ArrayList<AbstractStringCheckingResult>();
			try {
				// use dynamic only when static didn't find anything wrong, or when it crashed
				// note that logic is different compared to PreferDynamic case
				
				try {
					nodeResults.addAll(staticChecker.checkAbstractString(descriptor, configuration));
				} finally {
					if (nodeResults.isEmpty() && dynamicCheckerIsConfigured(configuration)) {
						nodeResults.addAll(dynamicChecker.checkAbstractString(descriptor, configuration));
					}
				}
			} catch (Exception e) {
				// should be able to proceed with next descriptors using static checker
				LOG.exception(e);
				nodeResults.add(new AbstractStringWarning("Error during checking: " + e.getMessage(), 
						descriptor.getPosition()));
			}
			results.addAll(nodeResults);
		}
		return results;
	}
	
}
