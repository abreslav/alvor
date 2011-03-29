package com.zeroturnaround.alvor.checkers.complex;

import java.util.ArrayList;
import java.util.Collection;

import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.HotspotCheckingResult;
import com.zeroturnaround.alvor.checkers.HotspotInfo;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.sqldynamic.DynamicSQLChecker;
import com.zeroturnaround.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
import com.zeroturnaround.alvor.common.StringHotspotDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.configuration.DataSourceProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

/**
 * This class combines static and dynamic checking
 * 
 */
public class ComplexChecker implements IAbstractStringChecker {

	private static final ILog LOG = Logs.getLog(ComplexChecker.class);
//	private static final ILog HOTSPOTS_LOG = Logs.getLog("Hotspots");
	
	private DynamicSQLChecker dynamicChecker = new DynamicSQLChecker();
	private SyntacticalSQLChecker staticChecker = new SyntacticalSQLChecker();

	@Override
	public Collection<HotspotCheckingResult> checkAbstractString(StringHotspotDescriptor hotspot, ProjectConfiguration configuration) throws CheckerException {
		
		if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.ALL_CHECKERS) {
			return checkStringNodeDescriptorsWithBothCheckers(hotspot, configuration);
		}
		else if (configuration.getCheckingStrategy() == ProjectConfiguration.CheckingStrategy.PREFER_DYNAMIC
				&& dynamicCheckerIsConfigured(configuration)) { 
			return checkStringNodeDescriptorPreferDynamic(hotspot, configuration);
		}
		else {
			return checkStringNodeDescriptorPreferStatic(hotspot, configuration);
		}
	}

	private Collection<HotspotCheckingResult> checkStringNodeDescriptorsWithBothCheckers(
			StringHotspotDescriptor hotspot, 
			ProjectConfiguration configuration) throws CheckerException {
		
		Collection<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		
		results.addAll(staticChecker.checkAbstractString(hotspot, configuration));
		
		if (dynamicCheckerIsConfigured(configuration)) {
			results.addAll(dynamicChecker.checkAbstractString(hotspot, configuration));
		}
		else {
			results.add(new HotspotInfo("SQL checker: testing database is not configured", null));
		}
		return results;
	}
	
	private boolean dynamicCheckerIsConfigured(ProjectConfiguration configuration) {
		DataSourceProperties props = configuration.getDefaultDataSource();
		return props != null && props.getUrl() != null && !props.getUrl().trim().isEmpty()
			&& props.getDriverName() != null && !props.getDriverName().trim().isEmpty();
	}
	
	private Collection<HotspotCheckingResult> checkStringNodeDescriptorPreferDynamic(
			StringHotspotDescriptor descriptor, 
			ProjectConfiguration configuration) {
		
		Collection<HotspotCheckingResult> nodeResults = new ArrayList<HotspotCheckingResult>();
		try {
			try {
				nodeResults.addAll(dynamicChecker.checkAbstractString(descriptor, configuration));
			} finally {
				if (!nodeResults.isEmpty()) {
					nodeResults.addAll(staticChecker.checkAbstractString(descriptor, configuration));
				}
			}
		} catch (Exception e) {
			LOG.exception(e);
			nodeResults.add(new HotspotInfo("Error during checking: " + e.getMessage(), 
					descriptor.getPosition()));
		}
		return nodeResults;
	}
	
	
	private Collection<HotspotCheckingResult> checkStringNodeDescriptorPreferStatic(
			StringHotspotDescriptor descriptor, 
			ProjectConfiguration configuration) {

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
		return nodeResults;
	}
}
