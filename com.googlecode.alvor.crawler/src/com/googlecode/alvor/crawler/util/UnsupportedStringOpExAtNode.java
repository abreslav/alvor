package com.googlecode.alvor.crawler.util;

import org.eclipse.jdt.core.dom.ASTNode;

import com.googlecode.alvor.common.UnsupportedStringOpEx;

public class UnsupportedStringOpExAtNode extends UnsupportedStringOpEx {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Brought ASTNode version to separate class to reduce dependencies
	 * @param message
	 * @param astNode
	 */
	public UnsupportedStringOpExAtNode(String message, ASTNode astNode) {
		super(message, ASTUtil.getPosition(astNode));
	}
}
