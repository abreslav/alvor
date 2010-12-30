package com.zeroturnaround.alvor.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationDescriptor {
	private String signature;
	private int argIndex;
	private MethodInvocation inv;
	
	public MethodInvocationDescriptor(MethodInvocation inv, int argIndex) {
		this.inv = inv;
		this.argIndex = argIndex;
	}
	
	public String getSignature() {
		if (signature == null) {
			// TODO check the string
			this.signature = inv.resolveMethodBinding().toString() + ":" + argIndex;
		}
		return signature;
	}
	
	public MethodInvocation getInvocation() {
		return inv;
	}
	
	public int getGetArgIndex() {
		return argIndex;
	}
}
