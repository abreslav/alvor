package com.zeroturnaround.alvor.cache;

public class FileRecord {
	private final String name;
	private final int batchNo;

	public FileRecord(String name, int batchNo) {
		this.name = name;
		this.batchNo = batchNo;
		
	}
	
	public String getName() {
		return name;
	}
	
	public int getBatchNo() {
		return batchNo;
	}
}
