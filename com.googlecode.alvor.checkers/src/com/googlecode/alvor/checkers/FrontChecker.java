package com.googlecode.alvor.checkers;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.configuration.CheckerConfiguration;
import com.googlecode.alvor.configuration.ProjectConfiguration;

/**
 * This checker finds suitable checker (according to connectionPattern and 
 * projectConfiguration) and delegates checking to that    
 * @author Aivar
 *
 */
public class FrontChecker {
	private static final String FALLBACK_CHECKER_NAME = "Generic-Syntax";
	
	// checkers indexed by (projectName + "$" + connectionPattern)
	private final Map<String, Map<String, IAbstractStringChecker>> checkers = new HashMap<String, Map<String, IAbstractStringChecker>>();

	public HotspotCheckingReport checkAbstractString (
			StringHotspotDescriptor descriptor,
			String projectName, ProjectConfiguration configuration) throws CheckerException {
		Map<String, IAbstractStringChecker> checkers = getMatchingCheckers(descriptor.getConnectionPattern(), projectName, configuration);
		
		if (checkers.size() > 1) {
			System.err.println("JEAAA: " + checkers.size());
		}
		
		
		HotspotCheckingReport report = new HotspotCheckingReport();
		for (Map.Entry<String, IAbstractStringChecker> entry : checkers.entrySet()) {
			String checkerName = entry.getKey();
			IAbstractStringChecker checker = entry.getValue();
			
			Collection<HotspotProblem> problems = checker.checkAbstractString(descriptor, projectName, configuration);
			report.addProblems(checkerName, problems);
			if (problems.isEmpty()) {
				report.addPassedChecker(checkerName);
			}
		}
		
		return report; 
	}
	
	private Map<String, IAbstractStringChecker> getMatchingCheckers(String connectionPattern, String projectName, 
			ProjectConfiguration projectConfiguration) {
		
		String checkerKey = projectName + "$" + connectionPattern;
		
		Map<String, IAbstractStringChecker> result = checkers.get(checkerKey);
		
		if (result == null) {
			result = new LinkedHashMap<String, IAbstractStringChecker>();
			
			// first search for first checker whose patterns match
			for (CheckerConfiguration checkerConf : projectConfiguration.getCheckers()) {
				String checkerName = checkerConf.getCheckerName();
				if (checkerConf.matchesPattern(connectionPattern)) {
					result.put(checkerName, CheckersManager.getCheckerByName(checkerName));
					break;
				}
			}
			
			// if no match, then add all default checkers
			for (CheckerConfiguration checkerConf : projectConfiguration.getCheckers()) {
				String checkerName = checkerConf.getCheckerName();
				if (checkerConf.isDefaultChecker()) {
					result.put(checkerName, CheckersManager.getCheckerByName(checkerName));
					break;
				}
			}
			
			// if still nothing, then add fall-back checker
			if (result.isEmpty()) {
				result.put(FALLBACK_CHECKER_NAME, CheckersManager.getCheckerByName(FALLBACK_CHECKER_NAME));
			}
			
			checkers.put(checkerKey, result);
		}
		
		return result;
	}
	
	public void resetCheckers() {
		checkers.clear();
	}
}
