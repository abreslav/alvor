package com.zeroturnaround.alvor.checkers;

import com.zeroturnaround.alvor.string.IPosition;

public class HotspotError extends HotspotCheckingResult {
	public HotspotError(String message, IPosition pos) {
		super(message, pos);
	}
}
