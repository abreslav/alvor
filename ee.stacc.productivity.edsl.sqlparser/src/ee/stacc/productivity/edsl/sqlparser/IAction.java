/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.Set;

public interface IAction {
	Set<IAbstractStack> process(int symbolNumber, IAbstractStack stack);
	boolean consumes();
	boolean isError();
}