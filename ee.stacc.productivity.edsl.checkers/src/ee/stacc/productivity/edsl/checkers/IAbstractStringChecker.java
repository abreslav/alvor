package ee.stacc.productivity.edsl.checkers;

import java.util.List;

public interface IAbstractStringChecker {
	void checkAbstractStrings(List<IStringNodeDescriptor> descriptors, ISQLErrorHandler errorHandler);
}
