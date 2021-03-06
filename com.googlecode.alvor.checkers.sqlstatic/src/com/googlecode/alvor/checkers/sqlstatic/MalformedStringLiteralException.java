/**
 * 
 */
package com.googlecode.alvor.checkers.sqlstatic;

import com.googlecode.alvor.string.IPosition;

/**
 * Thrown is a string literal is not a proper Java string literal
 * 
 * @author abreslav
 *
 */
@SuppressWarnings("serial")
public final class MalformedStringLiteralException extends RuntimeException {

	private IPosition literalPosition = null;
	
	public MalformedStringLiteralException(String message) {
		super(message);
	}

	/**
	 * Position of the malformed literal
	 */
	public IPosition getLiteralPosition() {
		return literalPosition;
	}

	public void setLiteralPosition(IPosition literalPosition) {
		this.literalPosition = literalPosition;
	}
	
}
