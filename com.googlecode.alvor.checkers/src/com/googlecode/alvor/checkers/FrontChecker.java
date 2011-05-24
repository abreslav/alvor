package com.googlecode.alvor.checkers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.logging.ILog;
import com.googlecode.alvor.common.logging.Logs;
import com.googlecode.alvor.configuration.CheckerConfiguration;
import com.googlecode.alvor.configuration.ProjectConfiguration;

public class FrontChecker implements IAbstractStringChecker {
	private static final String CHECKERS_ID = "com.googlecode.alvor.checkers.checkers";
	private static final String DEFAULT_DEFAULT_CHECKER_NAME = "Generic-Syntax";
	private static IAbstractStringChecker defaultDefaultChecker;
	private static final ILog LOG = Logs.getLog(FrontChecker.class); 
	// checkers indexed by connectionPatterns
	private final Map<String, IAbstractStringChecker> checkers = new HashMap<String, IAbstractStringChecker>();

	@Override
	public Collection<HotspotCheckingResult> checkAbstractString(
			StringHotspotDescriptor descriptor,
			ProjectConfiguration configuration) throws CheckerException {
		IAbstractStringChecker checker = getMatchingChecker(descriptor.getConnectionPattern(), configuration);
		if (checker == null) {
			HotspotCheckingResult result = new HotspotError("No suitable checker found", descriptor.getPosition());
			return Collections.singletonList(result);
		}
		else {
			return checker.checkAbstractString(descriptor, configuration);
		}
	}
	
	private IAbstractStringChecker getMatchingChecker(String connectionPattern, ProjectConfiguration conf) {
		IAbstractStringChecker result = checkers.get(connectionPattern);
		if (result == null) {
			IAbstractStringChecker defaultChecker = null;
			
			for (CheckerConfiguration checkerConf : conf.getCheckers()) {
				if (checkerConf.matchesPattern(connectionPattern)) {
					result = getCheckerByName(checkerConf.getCheckerName());
					break;
				}
				
				if (defaultChecker == null && checkerConf.isDefaultChecker()) {
					defaultChecker = getCheckerByName(checkerConf.getCheckerName());
				}
			}
			
			// if no checker matches by pattern, then use default checker
			if (result == null) {
				if (defaultChecker != null) {
					result = defaultChecker;
				}
				else {
					if (defaultDefaultChecker == null) {
						defaultDefaultChecker = getCheckerByName(DEFAULT_DEFAULT_CHECKER_NAME);
					}
					result = defaultDefaultChecker;
				}
			}
			
			checkers.put(connectionPattern, result);
		}
		
		return result;
	}

	public static IAbstractStringChecker getCheckerByName(String name) {
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(CHECKERS_ID);
			for (IConfigurationElement e : config) {
				if (e.getAttribute("name").equals(name)) {
					return (IAbstractStringChecker)e.createExecutableExtension("class");
				}
			}
			
			throw new IllegalArgumentException("Found no checker with name=" + name);
		} catch (CoreException e) {
			LOG.exception(e);
			throw new IllegalArgumentException(e);
		}
	}
	
	public void resetCheckers() {
		checkers.clear();
	}
}
