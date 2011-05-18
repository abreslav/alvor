package com.googlecode.alvor.string;


public interface IAbstractString {
	<R, D> R accept(IAbstractStringVisitor<? extends R, ? super D> visitor, D data);
	boolean isEmpty();
	boolean containsRecursion();
	
	IPosition getPosition();
}
