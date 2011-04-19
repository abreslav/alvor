package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public class StringHotspotDescriptor extends HotspotDescriptor {

	private IAbstractString abstractValue;

	public StringHotspotDescriptor(IPosition position,
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
		return getPosition().toString() + abstractValue.toString();
	}
}
