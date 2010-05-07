package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.string.AbstractStringEqualsVisitor;
import ee.stacc.productivity.edsl.string.IAbstractString;

public class AbstractStringEqualsVisitorExtended extends AbstractStringEqualsVisitor implements IAbstractStringVisitorExtended<Boolean, IAbstractString> {

	@Override
	public Boolean visitNamedString(NamedString abstracTString,
			IAbstractString data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Boolean visitRecursiveStringChoice(
			RecursiveStringChoice abstracTString, IAbstractString data) {
		// TODO Auto-generated method stub
		return false;
	}

}
