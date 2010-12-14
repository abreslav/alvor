package com.zeroturnaround.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;

@Deprecated
public class NameUsageLoopChoice extends NameUsage {
	private NameUsage startUsage;
	private NameUsage loopUsage;
	private ASTNode node;
	
	public NameUsageLoopChoice(ASTNode node, NameUsage startUsage, NameUsage loopUsage) {
		this.startUsage = startUsage;
		this.loopUsage = loopUsage;
		this.node = node;
	}

	public NameUsage getBaseUsage() {
		return startUsage;
	}
	
	public NameUsage getLoopUsage() {
		return loopUsage;
	}
	
	public ASTNode getNode() {
		return node;
	}
	
}
