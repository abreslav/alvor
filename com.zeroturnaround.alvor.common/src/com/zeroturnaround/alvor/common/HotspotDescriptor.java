package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

/**
 * Analyzer creates one NodeDescriptor for each hotspot
 * 
 */
public class HotspotDescriptor {
	private final IPosition position;
	
	public HotspotDescriptor(IPosition position) {
		this.position = position;
	}
	
	public IPosition getPosition() {
		return position;
	}
	
}
