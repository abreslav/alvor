package com.zeroturnaround.alvor.conntracker;

import com.zeroturnaround.alvor.string.IPosition;


public class ConnectionDescriptor {
	
	private String expression;
	private IPosition pos;

	public ConnectionDescriptor(IPosition pos, String expression) {
		this.pos = pos;
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}
	
	public IPosition getPos() {
		return pos;
	}
}
