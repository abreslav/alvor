package com.googlecode.alvor.checkers;

import com.googlecode.alvor.string.IPosition;

public class HotspotError extends HotspotCheckingResult {
	public HotspotError(String message, IPosition pos) {
		super(message, pos);
	}
}
