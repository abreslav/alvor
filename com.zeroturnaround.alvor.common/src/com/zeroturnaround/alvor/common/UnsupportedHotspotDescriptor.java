package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

public class UnsupportedHotspotDescriptor extends HotspotDescriptor {
	private String problemMessage;
	private IPosition errorPosition;
	
	public UnsupportedHotspotDescriptor(IPosition hotspotPosition, String problemMessage, 
			IPosition errorPosition) {
		this(hotspotPosition, problemMessage, errorPosition, 0);
	}

	public UnsupportedHotspotDescriptor(IPosition hotspotPosition, String problemMessage, 
			IPosition errorPosition, int markerId) {
		super(hotspotPosition, markerId);
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
