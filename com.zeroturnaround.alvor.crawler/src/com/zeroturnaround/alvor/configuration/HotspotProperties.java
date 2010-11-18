package com.zeroturnaround.alvor.configuration;

public class HotspotProperties {
	private final int argumentIndex;
	private final String methodName;
	private final String className;

	public HotspotProperties(String className, String methodName, int argumentIndex) {
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
}
