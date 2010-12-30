package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

public class UnsupportedStringOpEx extends UnsupportedOperationException {

	private IPosition position = null;
	private static final long serialVersionUID = 1L;

	public UnsupportedStringOpEx(String message, IPosition position) {
		super(message);
		this.position = position;
	}
	
//	public String getPositionedMessage() {
//		if (position == null) {
//			return getMessage();
//		}
//		else {
//			return getMessage() + " at: " + PositionUtil.getLineString(position);
//		}
//	}

	public IPosition getPosition() {
		return position;
	}
}
