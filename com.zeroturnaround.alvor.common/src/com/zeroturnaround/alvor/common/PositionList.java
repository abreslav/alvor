package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

public class PositionList {
	private final PositionList prev;
	private final IPosition pos;

	public PositionList(IPosition pos, PositionList prevLink) {
		this.prev = prevLink;
		this.pos = pos;
	}
	
	public IPosition getPosition() {
		return pos;
	}
	
	public boolean contains(IPosition pos) {
		return pos.equals(this.pos)
			|| prev != null && prev.contains(pos);
	}
	
	public PositionList getPrev() {
		return prev;
	}
}
