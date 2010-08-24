package com.zeroturnaround.alvor.conntracker;

import com.zeroturnaround.alvor.string.IPosition;


public class ConnectionDescriptor {
	
	private String expression;
	private String url;
//	private String username;
//	private String password;
	private IPosition pos;

	public ConnectionDescriptor(IPosition pos, String expression) {
		this.pos = pos;
		this.expression = expression;
	}

	@Override
	public String toString() {
		String s = expression;
		if (url != null) {
			s += ", URL: " + url;
		}
		return s;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public IPosition getPos() {
		return pos;
	}
}
