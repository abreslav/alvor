package com.zeroturnaround.alvor.checkers;

import java.util.Collection;
import java.util.List;

import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public interface IAbstractStringChecker {
	// TODO maybe better return the errors instead of passing handler ??
	
	Collection<AbstractStringCheckingResult> checkAbstractStrings(List<StringNodeDescriptor> descriptors, ProjectConfiguration configuration) throws CheckerException;
	
	/// result is true when no errors and checker is certain about it 
	Collection<AbstractStringCheckingResult> checkAbstractString(StringNodeDescriptor descriptor, ProjectConfiguration configuration) throws CheckerException;
}
