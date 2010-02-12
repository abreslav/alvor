package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.List;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;

public abstract class StaticSQLChecker implements IAbstractStringChecker {

	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			ISQLErrorHandler errorHandler) {
		for (IStringNodeDescriptor descriptor : descriptors) {
			List<String> errors = check(descriptor);
			for (String errorMessage : errors) {
				errorHandler.handleSQLError(errorMessage, 
						descriptor.getFile(), 
						descriptor.getCharStart(), 
						descriptor.getCharLength());
			}
		}
	}

	protected abstract List<String> check(IStringNodeDescriptor descriptor);

}
