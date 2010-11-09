package com.zeroturnaround.alvor.crawler;

import org.eclipse.jdt.core.dom.ASTNode;

/*
 * Used for recording chain of analysis steps 
 * from current analysis point back to initial hotspot expression
 */

public class ContextLink {
	private final ContextLink prevLink;
	private final ASTNode node;

	public ContextLink(ASTNode node, ContextLink prevLink) {
		this.prevLink = prevLink;
		this.node = node;
	}
	
	public ASTNode getNode() {
		return node;
	}
	
	public ContextLink getPrevLink() {
		return prevLink;
	}
}
