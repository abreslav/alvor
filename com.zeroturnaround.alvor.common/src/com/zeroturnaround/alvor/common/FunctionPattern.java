package com.zeroturnaround.alvor.common;

public class FunctionPattern extends StringPattern {

	public FunctionPattern(String className, String methodName, int argumentIndex) {
		super(className, methodName, argumentIndex);
	}
	
	
	public boolean equals(Object obj) {
		return super.equals(obj) && obj instanceof FunctionPattern;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + FunctionPattern.class.hashCode();
	}
}
