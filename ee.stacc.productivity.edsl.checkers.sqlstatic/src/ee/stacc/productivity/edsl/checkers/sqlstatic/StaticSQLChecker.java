package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.List;
import java.util.Map;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.string.StringConstant;

public abstract class StaticSQLChecker implements IAbstractStringChecker {

	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			ISQLErrorHandler errorHandler, Map<String, Object> options) {
		for (IStringNodeDescriptor descriptor : descriptors) {
			System.out.println(descriptor.getPosition((StringConstant) descriptor.getAbstractValue()));
			List<String> errors = check(descriptor);
			for (String errorMessage : errors) {
				errorHandler.handleSQLError(errorMessage, descriptor);
			}
		}
	}

	protected abstract List<String> check(IStringNodeDescriptor descriptor);

}
