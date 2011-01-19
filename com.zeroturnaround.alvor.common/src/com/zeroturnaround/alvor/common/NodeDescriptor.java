package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

/**
 * Analyzer creates one NodeDescriptor for each hotspot
 * 
 */
public class NodeDescriptor {
	private final IPosition position;
	
	public NodeDescriptor(IPosition position) {
		this.position = position;
	}
	
	public IPosition getPosition() {
		return position;
	}
}
