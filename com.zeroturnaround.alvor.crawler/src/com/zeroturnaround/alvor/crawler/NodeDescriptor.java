package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.checkers.INodeDescriptor;
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
