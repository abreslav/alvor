package com.googlecode.alvor.common;

import java.util.Map;

import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.IPosition;

public class FunctionPatternReference extends PatternReference {

	private final Map<Integer, IAbstractString> inputArguments;

	public FunctionPatternReference(IPosition pos, FunctionPattern pattern, 
			Map<Integer, IAbstractString> inputArguments) {
		super(pos, pattern);
		this.inputArguments = inputArguments;
		
	}
	
	public Map<Integer, IAbstractString> getInputArguments() {
		return inputArguments;
	}
	
	@Override
	public boolean containsRecursion() {
		for (Map.Entry<Integer, IAbstractString> argEntry : inputArguments.entrySet()) {
			if (argEntry.getValue().containsRecursion()) {
				return true;
			}
		}
		return false;
	}
}
