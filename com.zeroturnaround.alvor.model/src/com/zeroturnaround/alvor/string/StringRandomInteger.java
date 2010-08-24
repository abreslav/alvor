package com.zeroturnaround.alvor.string;

public class StringRandomInteger extends StringCharacterSet {
	public StringRandomInteger() {
		this(null);
	}
	
	public StringRandomInteger(IPosition pos) {
		super(pos, "0123456789");
	}
	
	public String toString() {
		return "([0123456789])+";
	}
	
	public String getExample() {
		return "666";
	}
}
