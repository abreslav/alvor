package com.zeroturnaround.alvor.common;

import org.eclipse.jdt.core.dom.ASTNode;

import com.zeroturnaround.alvor.common.util.PositionUtil;
import com.zeroturnaround.alvor.string.IPosition;

public class UnsupportedStringOpEx extends UnsupportedOperationException {

	private IPosition position = null;
	private static final long serialVersionUID = 1L;

	public UnsupportedStringOpEx(String message, ASTNode astNode) {
		super(message);
		if (astNode != null) {
			this.position = PositionUtil.getPosition(astNode);
		}
	}

	public UnsupportedStringOpEx(String message, IPosition position) {
		super(message);
		this.position = position;
	}
	
//	public String getPositionedMessage() {
//		if (position == null) {
//			return getMessage();
//		}
//		else {
//			return getMessage() + " at: " + PositionUtil.getLineString(position);
//		}
//	}

	public IPosition getPosition() {
		return position;
	}
}
