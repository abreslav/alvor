package ee.stacc.productivity.edsl.checkers;

import ee.stacc.productivity.edsl.string.IAbstractString;

public interface IStringNodeDescriptor extends INodeDescriptor {

	IAbstractString getAbstractValue();

}