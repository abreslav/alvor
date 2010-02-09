/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.Collections;
import java.util.Set;


public final class AfterErrorAction extends AbstractAction {
	@Override
	public Set<IAbstractStack> process(int symbolNumber,
			IAbstractStack stack) {
		return Collections.singleton(stack);
	}

	@Override
	public boolean isError() {
		return true;
	}
}