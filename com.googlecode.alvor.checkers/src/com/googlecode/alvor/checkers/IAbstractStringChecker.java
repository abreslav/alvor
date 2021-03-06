package com.googlecode.alvor.checkers;

import java.util.Collection;

import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.configuration.ProjectConfiguration;

public interface IAbstractStringChecker {
	Collection<HotspotProblem> checkAbstractString(StringHotspotDescriptor descriptor, String projectName, ProjectConfiguration configuration) throws CheckerException;
}
