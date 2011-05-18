package com.googlecode.alvor.cache;

import com.zeroturnaround.alvor.common.StringPattern;

public class PatternRecord {
	private final StringPattern pattern;
	private final int batchNo;
	private final int id;
	private final int patternRole;

	public PatternRecord(StringPattern pattern, int batchNo, int patternRole, int id) {
		this.pattern = pattern;
		this.batchNo = batchNo;
		this.patternRole = patternRole;
		this.id = id;
	}
	
	public int getBatchNo() {
		return batchNo;
	}
	
	public StringPattern getPattern() {
		return pattern;
	}
	
	public int getPatternRole() {
		return patternRole;
	}

	public int getId() {
		return id;
	}
	
	public boolean isPrimaryPattern() {
		return this.batchNo == 1;
	}
}
