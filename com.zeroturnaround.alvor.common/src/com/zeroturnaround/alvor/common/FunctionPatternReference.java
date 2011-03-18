package com.zeroturnaround.alvor.common;

import java.util.Map;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public class FunctionPatternReference extends PatternReference {

	private final Map<Integer, IAbstractString> inputArguments;

	public FunctionPatternReference(IPosition pos, StringPattern pattern, 
			Map<Integer, IAbstractString> inputArguments) {
		super(pos, pattern);
		this.inputArguments = inputArguments;
		
	}
	
	public Map<Integer, IAbstractString> getInputArguments() {
		return inputArguments;
	}
}
