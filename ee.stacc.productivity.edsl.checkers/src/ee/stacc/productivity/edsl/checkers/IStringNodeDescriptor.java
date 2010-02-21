package ee.stacc.productivity.edsl.checkers;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringConstant;

public interface IStringNodeDescriptor extends INodeDescriptor {

	IAbstractString getAbstractValue();
	
	/**
	 * @param literal a StringConstant instance
	 * @return position of the AST node from which the literal was created
	 */
	IPositionDescriptor getPosition(StringConstant literal);

}