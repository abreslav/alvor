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

/**
 * This checker finds suitable checker (according to connectionPattern and 
 * projectConfiguration) and delegates checking to that    
 * @author Aivar
 *
 */
public class FrontChecker implements IAbstractStringChecker {
	private static final String CHECKERS_ID = "com.googlecode.alvor.checkers.checkers";
	private static final String FALLBACK_CHECKER_NAME = "Generic-Syntax";
	private static IAbstractStringChecker fallbackChecker;
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
			CheckerConfiguration checkerConf = conf.getCheckerConfiguration(connectionPattern);
			
			if (checkerConf != null) {
				result = getCheckerByName(checkerConf.getCheckerName());
			}
			
			if (result == null) {
				result = getFallbackChecker();
			}
			
			checkers.put(connectionPattern, result);
		}
		
		return result;
	}
	
	public static IAbstractStringChecker getFallbackChecker() {
		if (fallbackChecker == null) {
			fallbackChecker = getCheckerByName(FALLBACK_CHECKER_NAME);
		}
		return fallbackChecker;
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
