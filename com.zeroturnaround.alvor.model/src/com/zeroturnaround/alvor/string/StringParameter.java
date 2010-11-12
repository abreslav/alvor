package com.zeroturnaround.alvor.string;


public class StringParameter extends PositionedString {

	private final int index;
	
	public StringParameter(int index) {
		this(null, index);
	}
	
	public StringParameter(IPosition pos, int index) {
		super(pos);
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		return visitor.visitStringParameter(this, data);
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return "Param(" + index + ")";
	}

	@Override
	public boolean containsRecursion() {
		return false;
	}
}
