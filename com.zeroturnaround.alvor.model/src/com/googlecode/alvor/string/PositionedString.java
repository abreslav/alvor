package com.googlecode.alvor.string;

public abstract class PositionedString implements IAbstractString {

	private final IPosition position;
	
	public PositionedString(IPosition position) {
		this.position = position;
	}

	@Override
	public IPosition getPosition() {
		return position;
	}

}
