package com.zeroturnaround.alvor.string;


public interface IAbstractString {
	<R, D> R accept(IAbstractStringVisitor<? extends R, ? super D> visitor, D data);
	boolean isEmpty();
	
	IPosition getPosition();
}
