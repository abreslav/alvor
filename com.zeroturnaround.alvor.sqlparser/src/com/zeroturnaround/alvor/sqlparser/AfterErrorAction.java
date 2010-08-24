/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

/**
 * An action available in the ERROR state.
 * 
 * @author abreslav
 *
 */
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