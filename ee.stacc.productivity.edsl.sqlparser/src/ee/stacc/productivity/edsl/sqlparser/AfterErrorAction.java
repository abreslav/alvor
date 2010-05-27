/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;


public final class AfterErrorAction extends AbstractAction {
	@Override
	public IParserStack process(IAbstractInputItem inputItem,
			IParserStack stack) {
		return stack;
	}

	@Override
	public boolean isError() {
		return true;
	}
}