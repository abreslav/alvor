package com.zeroturnaround.alvor.cache;

import com.zeroturnaround.alvor.common.StringPattern;

public class PatternRecord {
	private final StringPattern pattern;
	private final int batchNo;
	private final int id;

	public PatternRecord(StringPattern pattern, int batchNo, int id) {
		this.pattern = pattern;
		this.batchNo = batchNo;
		this.id = id;
	}
	
	public int getBatchNo() {
		return batchNo;
	}
	
	public StringPattern getPattern() {
		return pattern;
	}

	public int getId() {
		return id;
	}
}
