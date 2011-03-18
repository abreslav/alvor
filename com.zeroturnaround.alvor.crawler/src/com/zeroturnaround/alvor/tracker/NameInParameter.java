package com.zeroturnaround.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class NameInParameter extends NameUsage {
	private int index;
	private MethodDeclaration methodDecl;
	
	public NameInParameter(MethodDeclaration methodDecl, int index) {
		this.index = index;
		this.methodDecl = methodDecl;
	}
	
	public MethodDeclaration getMethodDecl() {
		return methodDecl;
	}

	@Deprecated
	public int getIndex() {
		return index;
	}
	
	public int getParameterNo() {
		return index+1;
	}
	
	public ASTNode getNode() {
		return (ASTNode)methodDecl.parameters().get(index);
	}
	
}
