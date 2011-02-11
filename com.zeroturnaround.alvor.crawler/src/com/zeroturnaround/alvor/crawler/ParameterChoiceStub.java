package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.StringChoice;

public class ParameterChoiceStub extends StringChoice {
	private final String className;
	private final String methodName;
	private final int argumentIndex;

	public ParameterChoiceStub(IPosition pos, String className, String methodName, int argumentIndex) {
		super(pos); // empty choice
		this.className = className;
		this.methodName = methodName;
		this.argumentIndex = argumentIndex;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public int getArgumentIndex() {
		return argumentIndex;
	}
}
