package ee.stacc.productivity.edsl.cmdline;

import java.util.List;

import ee.stacc.productivity.edsl.crawler.StringNodeDescriptor;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexicalChecker;
import ee.stacc.productivity.edsl.main.IAbstractStringChecker;
import ee.stacc.productivity.edsl.main.ISQLErrorHandler;
import ee.stacc.productivity.edsl.sqlparser.SQLSyntaxChecker;

public class StaticSQLChecker {

	public static final IAbstractStringChecker SQL_SYNTAX_CHECKER = new IAbstractStringChecker() {
		
		@Override
		public void checkAbstractStrings(List<StringNodeDescriptor> descriptors,
				ISQLErrorHandler errorHandler) {
			for (StringNodeDescriptor descriptor : descriptors) {
				List<String> errors = SQLSyntaxChecker.INSTANCE.check(descriptor.getAbstractValue());
				for (String errorMessage : errors) {
					errorHandler.handleSQLError(errorMessage, 
							descriptor.getFile(), 
							descriptor.getCharStart(), 
							descriptor.getCharLength());
				}
			}
		}
	};

	public static final IAbstractStringChecker SQL_LEXICAL_CHECKER = new IAbstractStringChecker() {
		
		@Override
		public void checkAbstractStrings(List<StringNodeDescriptor> descriptors,
				ISQLErrorHandler errorHandler) {
			for (StringNodeDescriptor descriptor : descriptors) {
				List<String> errors = SQLLexicalChecker.INSTANCE.check(descriptor.getAbstractValue());
				for (String errorMessage : errors) {
					errorHandler.handleSQLError(errorMessage, 
							descriptor.getFile(), 
							descriptor.getCharStart(), 
							descriptor.getCharLength());
				}
			}
		}
	};

}
