package com.googlecode.alvor.checkers;

import com.googlecode.alvor.string.IPosition;

public class HotspotProblem {
	public static enum ProblemType {ERROR, UNSUPPORTED};
	private final String message;
	private final IPosition pos;
	private final ProblemType problemType;
	
	public HotspotProblem(String message, IPosition pos, ProblemType problemType) {
		this.message = message;
		this.pos = pos;
		this.problemType = problemType;
	}
	
	public IPosition getPosition() {
		return pos;
	}

	public String getMessage() {
		return message;
	}

	public ProblemType getProblemType() {
		return problemType;
	}
}
