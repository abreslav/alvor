/**
 * 
 */
package ee.stacc.productivity.edsl.parser;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;

interface IStackFactory {
	IAbstractStack newStack(IParserState state);
}