package com.zeroturnaround.alvor.crawler;

import org.eclipse.jdt.core.dom.ASTNode;

import com.zeroturnaround.alvor.cache.PositionUtil;
import com.zeroturnaround.alvor.string.IPosition;

/*
 * Used for recording chain of analysis steps 
 * from current analysis point back to initial hotspot expression
 */

public class ContextLink {
	private final ContextLink prevLink;
	private final ASTNode node;
	private final IPosition pos;

	public ContextLink(ASTNode node, ContextLink prevLink) {
		this.prevLink = prevLink;
		this.node = node;
		this.pos = PositionUtil.getPosition(node);
	}
	
	public ASTNode getNode() {
		return node;
	}
	
	public ContextLink getPrevLink() {
		return prevLink;
	}
	
	public IPosition getPosition() {
		return pos;
	}
	
	public boolean contains(IPosition pos) {
		return pos.equals(this.pos)
			|| prevLink != null && prevLink.contains(pos);
	}
}
