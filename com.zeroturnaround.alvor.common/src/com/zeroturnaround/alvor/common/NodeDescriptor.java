package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

public class NodeDescriptor implements INodeDescriptor {
	private final IPosition position;
	
	public NodeDescriptor(IPosition position) {
		this.position = position;
	}
	
	@Override
	public IPosition getPosition() {
		return position;
	}
}
