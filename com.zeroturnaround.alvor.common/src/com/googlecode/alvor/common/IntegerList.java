package com.googlecode.alvor.common;


public class IntegerList {
	private final IntegerList prev;
	private final int val;

	public IntegerList(int val, IntegerList prevLink) {
		this.prev = prevLink;
		this.val = val;
	}
	
	public int getVal() {
		return val;
	}
	
	
	public boolean contains(int val) {
		return this.val == val	
			|| prev != null && prev.contains(val);
	}
	
	public IntegerList getPrev() {
		return prev;
	}
	
	public int getLength() {
		if (prev == null) {
			return 1;
		}
		else {
			return prev.getLength() + 1;
		}
	}
}
