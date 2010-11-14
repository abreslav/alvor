package com.zeroturnaround.alvor.string;

public class StringRecursion extends PositionedString {

	private final IPosition target;

	public StringRecursion(IPosition position, IPosition target) {
		super(position);
		this.target = target;
		
	}

	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsRecursion() {
		return true;
	}

	@Override
	public IPosition getPosition() {
		return null;
	}

	public IPosition getTarget() {
		return target;
	}
	
	@Override
	public String toString() {
		return "<<<" + target + ">>>";
	}
}
