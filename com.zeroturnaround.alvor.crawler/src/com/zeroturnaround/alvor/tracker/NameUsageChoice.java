package com.zeroturnaround.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;

public class NameUsageChoice extends NameUsage {
	private NameUsage thenUsage;
	private NameUsage elseUsage;
	private ASTNode commonParentNode;
	
	public NameUsageChoice(ASTNode commonParentNode, NameUsage thenUsage, NameUsage elseUsage) {
		this.thenUsage = thenUsage;
		this.elseUsage = elseUsage;
		this.commonParentNode = commonParentNode;
	}
	
	public NameUsage getElseUsage() {
		return elseUsage;
	}
	
	public NameUsage getThenUsage() {
		return thenUsage;
	}
	
	public ASTNode getCommonParentNode() {
		return commonParentNode;
	}
	
	public ASTNode getMainNode() {
		return commonParentNode;
	}
	
	@Override
	public int hashCode() {
		int result = 71;
		if (thenUsage != null) {
			result = result * 31 + thenUsage.hashCode();
		} 
		if (elseUsage != null) {
			result = result * 31 + elseUsage.hashCode();
		} 
		return result;
	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (obj == null || !(obj instanceof NameUsageChoice)) {
//			return false;
//		}
//		NameUsageChoice that = (NameUsageChoice)obj;
//		
//		if (   this.thenUsage == null && that.thenUsage != null
//			|| that.thenUsage == null && this.thenUsage != null
//			|| this.elseUsage == null && that.elseUsage != null
//			|| that.elseUsage == null && this.elseUsage != null) {
//			return false;
//		}
//		
//		return this.thenUsage.equals(that.thenUsage)
//			&& this.elseUsage.equals(that.elseUsage);
//	}

}
