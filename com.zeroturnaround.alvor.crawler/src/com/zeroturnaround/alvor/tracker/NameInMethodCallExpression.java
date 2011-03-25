package com.zeroturnaround.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class NameInMethodCallExpression extends NameUsage {
	private MethodInvocation inv;
	private Expression expression;
	
	public NameInMethodCallExpression(MethodInvocation inv, Expression expression) {
		this.inv = inv;
		this.expression = expression;
	}

	public MethodInvocation getInvocation() {
		return inv;
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	public ASTNode getMainNode() {
		return expression;
	}
	
}
