package com.googlecode.alvor.cache;

public class FileRecord {
	private final String name;
	private final int batchNo;
	private final int id;

	public FileRecord(int id, String name, int batchNo) {
		this.id = id;
		this.name = name;
		this.batchNo = batchNo;
		
	}
	
	public String getName() {
		return name;
	}
	
	public int getBatchNo() {
		return batchNo;
	}
	
	public int getId() {
		return id;
	}
}
