package com.googlecode.alvor.checkers;

import com.googlecode.alvor.string.IPosition;

public abstract class HotspotCheckingResult {
	private final String message;
	private final IPosition pos;
	
	public HotspotCheckingResult(String message, IPosition pos) {
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
