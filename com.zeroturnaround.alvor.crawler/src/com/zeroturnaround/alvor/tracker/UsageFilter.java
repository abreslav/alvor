package com.zeroturnaround.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;

public class UsageFilter extends NameUsage {
	private final boolean notNullCondition;
	private final NameUsage mainUsage;

	public UsageFilter(NameUsage mainUsage, boolean notNullCondition) {
		this.notNullCondition = notNullCondition;
		this.mainUsage = mainUsage;
	}
	
	@Override
	public ASTNode getMainNode() {
		return mainUsage.getMainNode();
	}
	
	public NameUsage getMainUsage() {
		return mainUsage;
	}

	public boolean hasNotNullCondition() {
		return notNullCondition;
	}
}
