package com.zeroturnaround.alvor.common;


// could be called also ArgumentPattern
public class HotspotPattern extends StringPattern {

	public HotspotPattern(String className, String methodName, int argumentIndex) {
		super(className, methodName, argumentIndex);
	}
	
	public boolean equals(Object obj) {
		return super.equals(obj) && obj instanceof HotspotPattern;
	}
	@Override
	public int hashCode() {
		return super.hashCode() + HotspotPattern.class.hashCode();
	}
}
