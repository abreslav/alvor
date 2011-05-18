package com.googlecode.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class NameInArgument extends NameUsage {
	MethodInvocation inv;
	int index;
	
	public NameInArgument(MethodInvocation inv, int index) {
		this.inv = inv;
		this.index = index;
	}

	public MethodInvocation getInvocation() {
		return inv;
	}
	
	public Expression getArgument() {
		return (Expression)inv.arguments().get(index);
	}
	
	public int getArgumentNo() {
		return index+1;
	}
	
	@Override
	public ASTNode getMainNode() {
		return getArgument();
	}
	
}
