package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.List;

import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.sqlparser.SQLSyntaxChecker;

public class SyntacticalSQLChecker extends StaticSQLChecker {

	@Override
	protected List<String> check(IStringNodeDescriptor descriptor) {
		return SQLSyntaxChecker.INSTANCE.check(descriptor.getAbstractValue());
	}

}
