package ee.stacc.productivity.edsl.sqlparser;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public interface IParseErrorHandler {

	IParseErrorHandler NONE = new IParseErrorHandler() {
		
		@Override
		public void unexpectedItem(IAbstractInputItem item) {
		}
		
		@Override
		public void other() {
		}
		
		@Override
		public void overabstraction() {
		}
	};
	
	void unexpectedItem(IAbstractInputItem item);
	void other();
	void overabstraction();
}
