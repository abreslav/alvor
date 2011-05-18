package com.googlecode.alvor.common;

import com.googlecode.alvor.string.IPosition;

public class FieldPatternReference extends PatternReference {

	public FieldPatternReference(IPosition pos, FieldPattern pattern) {
		super(pos, pattern);
	}
	@Override
	public boolean containsRecursion() {
		return false;
	}
}
