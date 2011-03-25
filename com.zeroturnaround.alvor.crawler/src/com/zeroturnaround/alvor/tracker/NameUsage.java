package com.zeroturnaround.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;

abstract public class NameUsage {
	abstract public ASTNode getMainNode();
	
	@Override
	public int hashCode() {
		return this.getMainNode().hashCode();
	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (!(obj instanceof NameUsage)) {
//			return false;
//		}
//		return (obj.getClass().equals(this.getClass())) 
//		// TODO if nodes are from different parses, then they don't equal
//		&& ((NameUsage)obj).getNodeDescribingUsage() == this.getNodeDescribingUsage();
//	}
}

