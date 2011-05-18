package com.googlecode.alvor.common;

import com.googlecode.alvor.string.IPosition;


public class HotspotPatternReference extends PatternReference {

	public HotspotPatternReference(IPosition pos, HotspotPattern pattern) {
		super(pos, pattern);
	}
	@Override
	public boolean containsRecursion() {
		return false;
	}
}
