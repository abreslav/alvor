package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public class StringNodeDescriptor extends NodeDescriptor implements IStringNodeDescriptor {

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