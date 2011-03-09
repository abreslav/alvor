package com.zeroturnaround.alvor.common;

import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.PositionedString;

public class PatternReference extends PositionedString {
	private final String className;
	private final String methodName;
	private final int argumentIndex;

	/* package */ PatternReference(IPosition pos, String className, String methodName, int argumentIndex) {
		super(pos); // empty choice
		this.className = className;
		this.methodName = methodName;
		this.argumentIndex = argumentIndex;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public int getArgumentIndex() {
		return argumentIndex;
	}

	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		throw new IllegalStateException();
	}

	@Override
	public boolean isEmpty() {
		throw new IllegalStateException();
	}

	@Override
	public boolean containsRecursion() {
		throw new IllegalStateException();
	}
	
}
