package com.googlecode.alvor.common;

import com.googlecode.alvor.string.DummyPosition;
import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.StringConstant;

public class EmptyStringConstant extends StringConstant {

	public EmptyStringConstant() {
		this(new DummyPosition());
	}

	public EmptyStringConstant(IPosition pos) {
		super(pos, "", "\"\"");
	}
	
}
