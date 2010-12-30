package com.zeroturnaround.alvor.common;


public class HotspotPattern {
	private int argumentIndex;
	private String methodName;
	private String className;

	public HotspotPattern(String className, String methodName, int argumentIndex) {
		this.className = className;
		this.methodName = methodName;
		this.argumentIndex = argumentIndex;
	}
	
	public int getArgumentIndex() {
		return argumentIndex;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public void setArgumentIndex(int argumentIndex) {
		this.argumentIndex = argumentIndex;
	}
	
	@Override
	public String toString() {
		return "Argument " + getArgumentIndex() + " of " + getClassName() + "." +  getMethodName();
	}
}
