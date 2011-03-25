package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

public class FieldPatternReference extends PatternReference {

	public FieldPatternReference(IPosition pos, FieldPattern pattern) {
		super(pos, pattern);
	}
	@Override
	public boolean containsRecursion() {
		return false;
	}
}
