package com.zeroturnaround.alvor.checkers;

import com.zeroturnaround.alvor.string.IPosition;

public class AbstractStringWarning extends AbstractStringError {

	public AbstractStringWarning(String message, IPosition pos) {
		super(message, pos);
	}
}
