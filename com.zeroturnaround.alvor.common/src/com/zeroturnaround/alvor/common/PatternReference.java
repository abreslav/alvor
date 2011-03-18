package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.PositionedString;

public class PatternReference extends PositionedString {
	private final StringPattern pattern;

	/* package */ PatternReference(IPosition pos, StringPattern pattern) {
		super(pos);
		this.pattern = pattern;
	}
	
	public StringPattern getPattern() {
		return pattern;
	}
	
	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		throw new IllegalStateException();
	}

	@Override
	public boolean isEmpty() {
		throw new IllegalStateException();
	}

	@Override
	public boolean containsRecursion() {
		return false;
	}
	
}
