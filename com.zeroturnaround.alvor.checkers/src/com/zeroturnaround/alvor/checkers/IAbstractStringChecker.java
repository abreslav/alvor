package com.zeroturnaround.alvor.checkers;

import java.util.Collection;

import com.zeroturnaround.alvor.common.StringHotspotDescriptor;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public interface IAbstractStringChecker {
	Collection<HotspotCheckingResult> checkAbstractString(StringHotspotDescriptor descriptor, ProjectConfiguration configuration) throws CheckerException;
}
