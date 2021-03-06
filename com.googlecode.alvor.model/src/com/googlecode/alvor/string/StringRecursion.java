package com.googlecode.alvor.string;

/*
 * Represents a string that recursively refers to one of it's ancestor string having same pos
 */
public class StringRecursion extends PositionedString {

	public StringRecursion(IPosition position) {
		super(position);
		assert ! DummyPosition.isDummyPosition(position); 
	}

	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		return visitor.visitStringRecursion(this, data);
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
	public String toString() {
		return "<<<" + getPosition() + ">>>";
	}
}
