package com.zeroturnaround.alvor.cache;

import com.zeroturnaround.alvor.string.IPosition;

public class UnsupportedStringOpEx extends UnsupportedOperationException {

	private IPosition position = null;
	private static final long serialVersionUID = 1L;

	public UnsupportedStringOpEx(String message) {
		this(message, null);
	}

	public UnsupportedStringOpEx(String message, IPosition position) {
		super(message);
		this.position = position;
	}

	public IPosition getPosition() {
		return position;
	}
}
