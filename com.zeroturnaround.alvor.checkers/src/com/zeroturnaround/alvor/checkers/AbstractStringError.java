package com.zeroturnaround.alvor.checkers;

import com.zeroturnaround.alvor.string.IPosition;

public class AbstractStringError extends AbstractStringCheckingResult {
	public AbstractStringError(String message, IPosition pos) {
		super(message, pos);
	}
}
