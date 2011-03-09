package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IPosition;

public class FunctionPatternReference extends PatternReference {

	public FunctionPatternReference(IPosition pos, String className,
			String methodName, int argumentIndex) {
		super(pos, className, methodName, argumentIndex);
		
	}
}
