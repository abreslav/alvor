/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.Collections;
import java.util.Set;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;


public final class AfterErrorAction extends AbstractAction {
	@Override
	public Set<IAbstractStack> process(IAbstractInputItem inputItem,
			IAbstractStack stack) {
		return Collections.singleton(stack);
	}

	@Override
	public boolean isError() {
		return true;
	}
}