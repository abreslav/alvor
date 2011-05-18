package com.googlecode.alvor.common;

public class FunctionPattern extends StringPattern {

	public FunctionPattern(String className, String methodName, String argumentTypes, int argumentIndex) {
		super(className, methodName, argumentTypes, argumentIndex);
	}
	
	
	public boolean equals(Object obj) {
		return super.equals(obj) && obj instanceof FunctionPattern;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + FunctionPattern.class.hashCode();
	}
}
