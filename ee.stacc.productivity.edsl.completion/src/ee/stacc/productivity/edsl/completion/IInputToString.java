/**
 * 
 */
package ee.stacc.productivity.edsl.completion;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public interface IInputToString {
	String toString(IAbstractInputItem item);
}