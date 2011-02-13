package com.zeroturnaround.alvor.cache;

public class PatternRecord {
	private final String className;
	private final String methodName;
	private final int argumentIndex;
	private final int batchNo;

	public PatternRecord(String className, String methodName, int argIndex, 
			int batchNo) {
				this.className = className;
				this.methodName = methodName;
				this.argumentIndex = argIndex;
				this.batchNo = batchNo;
	}
	
	public int getArgumentIndex() {
		return argumentIndex;
	}
	
	public int getBatchNo() {
		return batchNo;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}
}
