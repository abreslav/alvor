package com.zeroturnaround.alvor.checkers;

import com.zeroturnaround.alvor.string.IPosition;

public abstract class AbstractStringCheckingResult {
	private final String message;
	private final IPosition pos;
	
	public AbstractStringCheckingResult(String message, IPosition pos) {
		this.message = message;
		this.pos = pos;
	}
	
	public IPosition getPosition() {
		return pos;
	}

	public String getMessage() {
		return message;
	}

}
