package com.zeroturnaround.alvor.checkers;

import java.util.List;
import java.util.Map;

public interface IAbstractStringChecker {
	void checkAbstractStrings(List<IStringNodeDescriptor> descriptors, ISQLErrorHandler errorHandler, Map<String, String> options) throws CheckerException;
	void checkAbstractString(IStringNodeDescriptor descriptor, ISQLErrorHandler errorHandler, Map<String, String> options) throws CheckerException;
}
