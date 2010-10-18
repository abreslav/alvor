package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.string.IPosition;

public class UnsupportedNodeDescriptor extends NodeDescriptor {
	private String problemMessage;
	private IPosition errorPosition;
	
	public UnsupportedNodeDescriptor(IPosition hotspotPosition, String problemMessage, 
			IPosition errorPosition) {
		super(hotspotPosition);
		this.problemMessage = problemMessage;
		this.errorPosition = errorPosition;
	}

	public String getProblemMessage() {
		return problemMessage;
	}
	
	public IPosition getErrorPosition() {
		return errorPosition;
	}

}
