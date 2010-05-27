/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;


import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public interface IAction {
	IParserStack process(IAbstractInputItem inputItem, IParserStack stack);
	boolean consumes();
	boolean isError();
}