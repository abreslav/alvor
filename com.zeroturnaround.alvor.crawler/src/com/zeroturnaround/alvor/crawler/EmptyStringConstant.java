package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.cache.DummyPosition;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.StringConstant;

public class EmptyStringConstant extends StringConstant {

	public EmptyStringConstant() {
		this(new DummyPosition());
	}

	public EmptyStringConstant(IPosition pos) {
		super(pos, "", "\"\"");
	}
	
}
