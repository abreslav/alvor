/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;



public interface IParserStack extends IParserStackLike {
	IParserState top();
	IParserStack push(IParserState state);
	IParserStack pop(int count);
	
}