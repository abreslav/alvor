package com.googlecode.alvor.common;

public class StringPattern {
	private int argumentNo;
	private String methodName;
	private String argumentTypes;
	private String className;

	protected StringPattern(String className, String methodName, String argumentTypes, int argumentNo) {
		if (argumentNo == 0 || argumentNo < -1) {
			throw new IllegalArgumentException("argumentNo must be positive or -1");
		}
		this.className = className;
		this.methodName = methodName;
		this.argumentTypes = argumentTypes;
		this.argumentNo = argumentNo;
	}
	
	public int getArgumentNo() {
		return argumentNo;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	@Override
	public String toString() {
		return "Argument " + getArgumentNo() + " of " + getClassName() + "." +  getMethodName();
	}
	
	public String getArgumentTypes() {
		return argumentTypes;
	}
	
	@Override
	public int hashCode() {
		int result = 22;
		result = result * 31 + className.hashCode();
		result = result * 31 + methodName.hashCode();
		result = result * 31 + argumentTypes.hashCode();
		result = result * 31 + argumentNo;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj 
		|| obj != null 
		   && obj instanceof StringPattern 
		   && this.hashCode() == obj.hashCode();
	}

}
