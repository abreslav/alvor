package com.zeroturnaround.alvor.checkers.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.zeroturnaround.alvor.checkers.AbstractStringCheckerManager;
import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.HotspotCheckingResult;
import com.zeroturnaround.alvor.checkers.HotspotError;
import com.zeroturnaround.alvor.checkers.HotspotInfo;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.sqldynamic.DynamicSQLChecker;
import com.zeroturnaround.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.configuration.DataSourceProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

/**
 * This class combines static and dynamic checking
 * 
 */
public class ComplexChecker {

	private static final ILog LOG = Logs.getLog(ComplexChecker.class);
	private static final ILog HOTSPOTS_LOG = Logs.getLog("Hotspots");
	

	public Collection<HotspotCheckingResult> checkNodeDescriptors(Collection<HotspotDescriptor> hotspots, 
		ProjectConfiguration configuration, IProgressMonitor monitor) {
		
		int unsupportedCount = 0;
		
		Collection<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		List<IAbstractStringChecker> checkers = AbstractStringCheckerManager.INSTANCE.getCheckers();
		
		List<StringNodeDescriptor> validHotspots = new ArrayList<StringNodeDescriptor>();
		for (HotspotDescriptor hotspot : hotspots) {
			if (hotspot instanceof StringNodeDescriptor) {
				validHotspots.add((StringNodeDescriptor) hotspot);
				// TODO this makes log quite big
				HOTSPOTS_LOG.message("STRING node desc, file=" + PositionUtil.getLineString(hotspot.getPosition())
						+ ", str=" + ((StringNodeDescriptor) hotspot).getAbstractValue());
				
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
				results.add(new HotspotInfo(msg, hotspot.getPosition()));
			}
			else {
				throw new IllegalArgumentException("Unknown type of INodeTypeDescriptor: " + hotspot.getClass().getName());
			}
		}
		results.addAll(checkStringNodeDescriptors(validHotspots, checkers, configuration, monitor));
		
		LOG.message("Processed " + hotspots.size() + " node descriptors, "
				+ validHotspots.size() + " of them with valid abstract strings, "
				+ "unsupported cases: " + unsupportedCount);

		return results;
	}

	private Collection<HotspotCheckingResult> checkStringNodeDescriptors(
			List<StringNodeDescriptor> hotspots, 
			List<IAbstractStringChecker> checkers, 
			ProjectConfiguration configuration, IProgressMonitor monitor) {
		
		Timer timer = new Timer();
		timer.start("TIMER checking");
		
		Collection<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		
		if (!dynamicCheckerIsConfigured(configuration)) {
			results.add(new HotspotInfo("Alvor: Test-database is not configured, SQL testing is not performed",
					null));
		}
		
		try {
			if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.ALL_CHECKERS) {
				assert LOG.message("Checking with all checkers");
				results.addAll(checkStringNodeDescriptorsWithAllCheckers(hotspots,checkers, configuration, monitor));
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
					results.addAll(checkStringNodeDescriptorsPreferDynamic(hotspots, dynamicChecker, staticChecker, configuration, monitor));
				}
				else if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.PREFER_STATIC) { 
					assert LOG.message("Prefering static checker");
					results.addAll(checkStringNodeDescriptorsPreferStatic(hotspots, dynamicChecker, staticChecker, configuration, monitor));
				}
				else {
					throw new CheckerException("Unknown checking strategy", null);
				}
			}
		}
		catch (CheckerException e) {
			results.add(new HotspotError("SQL checker exception: " + e.getMessage(), e.getPosition()));
		}
		
		timer.printTime();
		return results;
	}
	
	private Collection<HotspotCheckingResult> checkStringNodeDescriptorsWithAllCheckers(
			List<StringNodeDescriptor> hotspots, 
			List<IAbstractStringChecker> checkers, 
			ProjectConfiguration configuration, IProgressMonitor monitor) throws CheckerException {
		
		Collection<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		for (IAbstractStringChecker checker : checkers) {
			if (checker instanceof DynamicSQLChecker 
					&& !dynamicCheckerIsConfigured(configuration)) {
				results.add(new HotspotInfo("SQL checker: testing database is not configured", null));
				
			} else {
				Timer timer = new Timer();
				timer.start("TIMER checker=" + checker.getClass().getName());
				results.addAll(checker.checkAbstractStrings(hotspots, configuration));
				timer.printTime();
			}
			
			checkMonitor(monitor);
		}
		return results;
	}
	
	private boolean dynamicCheckerIsConfigured(ProjectConfiguration configuration) {
		DataSourceProperties props = configuration.getDefaultDataSource();
		return props != null && props.getUrl() != null && !props.getUrl().trim().isEmpty()
			&& props.getDriverName() != null && !props.getDriverName().trim().isEmpty();
	}
	
	private Collection<HotspotCheckingResult> checkStringNodeDescriptorsPreferDynamic(
			List<StringNodeDescriptor> descriptors, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			ProjectConfiguration configuration, IProgressMonitor monitor) {
		
		Collection<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		for (StringNodeDescriptor descriptor : descriptors) {
			Collection<HotspotCheckingResult> nodeResults = new ArrayList<HotspotCheckingResult>();
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
				nodeResults.add(new HotspotInfo("Error during checking: " + e.getMessage(), 
						descriptor.getPosition()));
			}
			results.addAll(nodeResults);
			checkMonitor(monitor);
		}
		return results;
	}
	
	
	private Collection<HotspotCheckingResult> checkStringNodeDescriptorsPreferStatic(
			List<StringNodeDescriptor> descriptors, 
			IAbstractStringChecker dynamicChecker, 
			IAbstractStringChecker staticChecker, 
			ProjectConfiguration configuration, IProgressMonitor monitor) {
		
		Collection<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		for (StringNodeDescriptor descriptor : descriptors) {
			Collection<HotspotCheckingResult> nodeResults = new ArrayList<HotspotCheckingResult>();
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
				nodeResults.add(new HotspotInfo("Error during checking: " + e.getMessage(), 
						descriptor.getPosition()));
			}
			results.addAll(nodeResults);
			checkMonitor(monitor);
		}
		return results;
	}
	
	private void checkMonitor(IProgressMonitor monitor) {
		if (monitor != null) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			else {
				monitor.worked(1);
			}
		}
	}
	
}
