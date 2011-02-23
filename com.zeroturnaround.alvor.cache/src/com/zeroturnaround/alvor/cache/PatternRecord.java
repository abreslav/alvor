package com.zeroturnaround.alvor.cache;

import com.zeroturnaround.alvor.common.HotspotPattern;

public class PatternRecord {
	private final HotspotPattern pattern;
	private final int batchNo;

	public PatternRecord(HotspotPattern pattern, int batchNo) {
				this.pattern = pattern;
				this.batchNo = batchNo;
	}
	
	public int getBatchNo() {
		return batchNo;
	}
	
	public HotspotPattern getPattern() {
		return pattern;
	}
	
}
