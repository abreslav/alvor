package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.string.IAbstractStringVisitor;

public interface IAbstractStringVisitorExtended<R, D> extends IAbstractStringVisitor<R, D> {

	R visitRecursiveStringChoice(RecursiveStringChoice abstracTString, D data);
	R visitNamedString(NamedString abstracTString, D data);
}
