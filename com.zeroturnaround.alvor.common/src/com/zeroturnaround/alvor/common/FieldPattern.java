package com.zeroturnaround.alvor.common;

public class FieldPattern extends StringPattern {

	public FieldPattern(String className, String fieldName) {
		super(className, fieldName, "", -1);
	}
	
	public String getFieldName() {
		return getMethodName();
	}
}
