/**
 * 
 */
package ee.stacc.productivity.edsl.checkers.sqlstatic;

import ee.stacc.productivity.edsl.string.IPosition;

@SuppressWarnings("serial")
public final class MalformedStringLiteralException extends RuntimeException {

	private IPosition literalPosition = null;
	
	public MalformedStringLiteralException(String message) {
		super(message);
	}

	public IPosition getLiteralPosition() {
		return literalPosition;
	}

	public void setLiteralPosition(IPosition literalPosition) {
		this.literalPosition = literalPosition;
	}
	
}