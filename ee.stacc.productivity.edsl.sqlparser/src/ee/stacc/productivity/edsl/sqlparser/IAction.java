/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.Set;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public interface IAction {
	Set<IAbstractStack> process(IAbstractInputItem inputItem, IAbstractStack stack);
	boolean consumes();
	boolean isError();
}