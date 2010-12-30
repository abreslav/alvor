package com.zeroturnaround.alvor.util;

import org.eclipse.jdt.core.dom.ASTNode;

import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;

public class UnsupportedStringOpExAtNode extends UnsupportedStringOpEx {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Brought ASTNode version to separate class to reduce dependencies
	 * @param message
	 * @param astNode
	 */
	public UnsupportedStringOpExAtNode(String message, ASTNode astNode) {
		super(message, PositionUtil.getPosition(astNode));
	}
}
