package com.googlecode.alvor.common;

import com.googlecode.alvor.string.IPosition;

public class UnsupportedHotspotDescriptor extends HotspotDescriptor {
	private String problemMessage;
	private IPosition errorPosition;
	
	public UnsupportedHotspotDescriptor(IPosition hotspotPosition, String problemMessage, 
			IPosition errorPosition) {
		super(hotspotPosition);
		assert problemMessage != null;
		this.problemMessage = problemMessage;
		this.errorPosition = errorPosition;
	}

	public String getProblemMessage() {
		return problemMessage;
	}
	
	public IPosition getErrorPosition() {
		return errorPosition;
	}

	@Override
	public String toString() {
		return getPosition().toString() + ", unsupported: " + problemMessage;
	}
}
