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
	
	public int getArgumentNo() {
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
		return "Argument " + getArgumentNo() + " of " + getClassName() + "." +  getMethodName();
	}
	
	@Override
	public int hashCode() {
		int result = 22;
		result = result * 31 + className.hashCode();
		result = result * 31 + methodName.hashCode();
		result = result * 31 + argumentIndex;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj 
		|| obj != null 
		   && obj instanceof HotspotPattern 
		   && this.hashCode() == obj.hashCode();
	}
}
