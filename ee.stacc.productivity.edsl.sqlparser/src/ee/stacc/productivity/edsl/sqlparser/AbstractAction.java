/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

public abstract class AbstractAction implements IAction {
	@Override
	public boolean consumes() {
		return false;
	}
	
	@Override
	public boolean isError() {
		return false;
	}
}