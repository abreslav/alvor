package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.string.IPosition;

public class UnsupportedNodeDescriptor extends NodeDescriptor {
	private String problemMessage;
	
	public UnsupportedNodeDescriptor(IPosition position, String problemMessage) {
		super(position);
		this.problemMessage = problemMessage;
	}

	public String getProblemMessage() {
		return problemMessage;
	}

}
