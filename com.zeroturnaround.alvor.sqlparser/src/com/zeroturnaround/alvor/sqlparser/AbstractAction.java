/**
 * 
 */
package com.zeroturnaround.alvor.sqlparser;

/**
 * A base class for actions (might have been called ActionAdapter, in Swing style) 
 * 
 * @author abreslav
 *
 */
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
