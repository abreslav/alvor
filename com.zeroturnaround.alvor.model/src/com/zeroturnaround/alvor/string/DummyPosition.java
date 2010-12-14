package com.zeroturnaround.alvor.string;


public class DummyPosition extends Position {
	public static final String DUMMY_PATH = "__dummy__"; 
	public DummyPosition() {
		super(DummyPosition.DUMMY_PATH , 1, 2);
	}
	public static boolean isDummyPosition(IPosition pos) {
		return (pos instanceof DummyPosition)
			|| pos.getPath().equals(DummyPosition.DUMMY_PATH);
	}
}
