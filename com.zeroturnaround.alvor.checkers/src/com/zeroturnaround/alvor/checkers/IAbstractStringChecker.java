package com.zeroturnaround.alvor.checkers;

import java.util.List;

import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public interface IAbstractStringChecker {
	// TODO maybe better return the errors instead of passing handler ??
	
	void checkAbstractStrings(List<StringNodeDescriptor> descriptors, ISQLErrorHandler errorHandler, ProjectConfiguration configuration) throws CheckerException;
	
	/// result is true when no errors and checker is certain about it 
	boolean checkAbstractString(StringNodeDescriptor descriptor, ISQLErrorHandler errorHandler, ProjectConfiguration configuration) throws CheckerException;
}
