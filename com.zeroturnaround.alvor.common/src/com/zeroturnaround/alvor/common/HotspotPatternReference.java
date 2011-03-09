package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;


public class HotspotPatternReference extends PatternReference {

	public HotspotPatternReference(IPosition pos, String className, String methodName,
			int argumentIndex) {
		super(pos, className, methodName, argumentIndex);
	}
}
