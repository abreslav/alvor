package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.List;

import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexicalChecker;

/*
 * This class is osolete
 */
public class LexicalSQLChecker extends StaticSQLChecker {

	@Override
	protected List<String> check(IStringNodeDescriptor descriptor) {
		return SQLLexicalChecker.INSTANCE.check(descriptor.getAbstractValue());
	}

}
