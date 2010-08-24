package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;

public interface IAbstractStringVisitorExtended<R, D> extends IAbstractStringVisitor<R, D> {

	R visitRecursiveStringChoice(RecursiveStringChoice abstracTString, D data);
	R visitNamedString(NamedString abstracTString, D data);
}
