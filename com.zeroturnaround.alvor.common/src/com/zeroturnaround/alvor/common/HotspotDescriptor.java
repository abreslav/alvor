package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

/**
 * Analyzer creates one NodeDescriptor for each hotspot
 * 
 */
public class HotspotDescriptor {
	private final IPosition position;
	private final int markerId;
	
	public HotspotDescriptor(IPosition position, int markerId) {
		this.position = position;
		this.markerId = markerId;
	}
	
	public IPosition getPosition() {
		return position;
	}
	
	public int getMarkerId() {
		return markerId;
	}
}
