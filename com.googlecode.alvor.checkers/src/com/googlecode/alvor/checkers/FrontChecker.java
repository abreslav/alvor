package com.googlecode.alvor.checkers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.configuration.DataSourceProperties;
import com.googlecode.alvor.configuration.ProjectConfiguration;

public class FrontChecker implements IAbstractStringChecker {
	// checkers indexed by database descriptors
	private final Map<String, IAbstractStringChecker> checkers = new HashMap<String, IAbstractStringChecker>();

	@Override
	public Collection<HotspotCheckingResult> checkAbstractString(
			StringHotspotDescriptor descriptor,
			ProjectConfiguration configuration) throws CheckerException {
		// match descript
		return null;
	}
	
	private IAbstractStringChecker getMatchingChecker(String databaseDescriptor, ProjectConfiguration conf) {
		IAbstractStringChecker result = checkers.get(databaseDescriptor);
		if (result == null) {
//			result = AbstractStringCheckerManager.INSTANCE.getCheckers();
		}
		return result;
	}

}
