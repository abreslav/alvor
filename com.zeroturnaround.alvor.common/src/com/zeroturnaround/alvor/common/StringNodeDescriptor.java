package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public class StringNodeDescriptor extends NodeDescriptor {

	private IAbstractString abstractValue;

	public StringNodeDescriptor(IPosition position,
			IAbstractString abstractValue) {
		super(position);
		if (abstractValue == null)
			throw new IllegalStateException();
		this.abstractValue = abstractValue;
	}

	public IAbstractString getAbstractValue() {
		return this.abstractValue;
	}
	
	public void setAbstractValue(IAbstractString abstractValue) {
		this.abstractValue = abstractValue;
	}
	
	@Override
	public String toString() {
		return getPosition().toString();
	}
}
