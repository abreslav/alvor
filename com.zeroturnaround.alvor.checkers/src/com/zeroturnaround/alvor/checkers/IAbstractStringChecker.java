package com.zeroturnaround.alvor.checkers;

import java.util.Collection;

import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public interface IAbstractStringChecker {
	Collection<HotspotCheckingResult> checkAbstractString(StringNodeDescriptor descriptor, ProjectConfiguration configuration) throws CheckerException;
}
