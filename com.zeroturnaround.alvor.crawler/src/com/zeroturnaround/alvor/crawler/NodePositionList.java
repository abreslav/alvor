package com.zeroturnaround.alvor.crawler;

import org.eclipse.jdt.core.dom.ASTNode;

import com.zeroturnaround.alvor.common.PositionList;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.string.IPosition;

public class NodePositionList extends PositionList {
	public NodePositionList(IPosition pos, NodePositionList prevLink) {
		super(pos, prevLink);
	}
	
	public NodePositionList(ASTNode node, NodePositionList prevLink) {
		super(ASTUtil.getPosition(node), prevLink);
	}
}
