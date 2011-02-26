package com.zeroturnaround.alvor.crawler.string;

import java.util.List;

import com.zeroturnaround.alvor.common.FunctionPattern;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.PositionedString;

public class StringFunctionApplicationStub extends PositionedString {

	private final FunctionPattern pattern;
	private final List<IAbstractString> arguments;

	public StringFunctionApplicationStub(IPosition position, FunctionPattern pattern,
			List<IAbstractString> arguments) {
		super(position);
		this.pattern = pattern;
		this.arguments = arguments;
	}

	public List<IAbstractString> getArguments() {
		return arguments;
	}
	
	public FunctionPattern getFunctionPattern() {
		return pattern;
	}
	
	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsRecursion() {
		// TODO Auto-generated method stub
		return false;
	}

	
	
}
