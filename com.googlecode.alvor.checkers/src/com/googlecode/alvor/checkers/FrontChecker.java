package com.googlecode.alvor.checkers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
public class FrontChecker implements IAbstractStringChecker {
	private static final String FALLBACK_CHECKER_NAME = "Generic-Syntax";
	
	// checkers indexed by (projectName + "$" + connectionPattern)
	private final Map<String, List<IAbstractStringChecker>> checkers = new HashMap<String, List<IAbstractStringChecker>>();

	@Override
	public Collection<HotspotProblem> checkAbstractString(
			StringHotspotDescriptor descriptor,
			String projectName, ProjectConfiguration configuration) throws CheckerException {
		List<IAbstractStringChecker> checkers = getMatchingCheckers(descriptor.getConnectionPattern(), projectName, configuration);
		
		// if one of the checkers succeeds, then return success
		// otherwise return messages from last checker who fails
		
		// TODO better approach needed here, trying multiple checkers is not so good solution.
		// Normally there should be only one matching checker for each hotspot
		// or several checkers that must ALL pass. 
		
		Collection<HotspotProblem> problems = new ArrayList<HotspotProblem>();
		for (IAbstractStringChecker checker : checkers) {
			problems = checker.checkAbstractString(descriptor, projectName, configuration);
			if (problems.isEmpty()) {
				// found the "right" checker
				return problems;
			}
		}
		
		return problems; 
		
		// TODO it should be visible, which checker was used for checking
	}
	
	private List<IAbstractStringChecker> getMatchingCheckers(String connectionPattern, String projectName, 
			ProjectConfiguration projectConfiguration) {
		
		String checkerKey = projectName + "$" + connectionPattern;
		
		List<IAbstractStringChecker> result = checkers.get(checkerKey);
		
		if (result == null) {
			result = new ArrayList<IAbstractStringChecker>();
			
			// first search for first checker whose patterns match
			for (CheckerConfiguration checkerConf : projectConfiguration.getCheckers()) {
				if (checkerConf.matchesPattern(connectionPattern)) {
					result.add(CheckersManager.getCheckerByName(checkerConf.getCheckerName()));
					break;
				}
			}
			
			// if no match, then add all default checkers
			for (CheckerConfiguration checkerConf : projectConfiguration.getCheckers()) {
				if (checkerConf.isDefaultChecker()) {
					result.add(CheckersManager.getCheckerByName(checkerConf.getCheckerName()));
					break;
				}
			}
			
			// if still nothing, then add fall-back checker
			if (result.isEmpty()) {
				result.add(CheckersManager.getCheckerByName(FALLBACK_CHECKER_NAME));
			}
			
			checkers.put(checkerKey, result);
		}
		
		return result;
	}
	
	public void resetCheckers() {
		checkers.clear();
	}
}
