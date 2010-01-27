/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.Set;


public interface IAbstractStack {
	IParserState top();
	IAbstractStack push(IParserState state);
	Set<IAbstractStack> pop(int count);
}