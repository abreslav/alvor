package com.zeroturnaround.alvor.checkers;

import com.zeroturnaround.alvor.string.IPosition;

public class CheckerException extends Exception {
	private static final long serialVersionUID = 1L;
	private IPosition pos;

	public CheckerException(String message, IPosition pos) {
		super(message);
		this.pos = pos;
	}
	
	public IPosition getPosition() {
		return pos;
	}
}
