package com.zeroturnaround.alvor.crawler;

import org.eclipse.jdt.core.dom.MethodInvocation;

import com.zeroturnaround.alvor.cache.IMethodInvocationDescriptor;

public class MethodInvocationDescriptor implements IMethodInvocationDescriptor {
	private String signature;
	private int argIndex;
	private MethodInvocation inv;
	
	public MethodInvocationDescriptor(MethodInvocation inv, int argIndex) {
		this.inv = inv;
		this.argIndex = argIndex;
	}
	
	/* (non-Javadoc)
	 * @see com.zeroturnaround.alvor.cache.IMethodInvocationDescriptor#getSignature()
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see com.zeroturnaround.alvor.cache.IMethodInvocationDescriptor#getGetArgIndex()
	 */
	@Override
	public int getGetArgIndex() {
		return argIndex;
	}
}
