package ee.stacc.productivity.edsl.main;

import java.util.List;

import ee.stacc.productivity.edsl.crawler.StringNodeDescriptor;

public interface IAbstractStringChecker {
	void checkAbstractStrings(List<StringNodeDescriptor> descriptors, ISQLErrorHandler errorHandler);
}
