package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.PositionedString;
import com.zeroturnaround.alvor.string.StringConstant;

public abstract class PatternReference extends PositionedString {
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
		// temporary hack
		//return visitor.visitStringConstant(new StringConstant(this.getPosition(), "", "\"\""), data);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}
