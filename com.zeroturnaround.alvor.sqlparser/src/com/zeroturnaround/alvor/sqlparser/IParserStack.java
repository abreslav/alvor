/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;


/**
 * Represents an (immutable) single stack of parsing states
 * 
 * @author abreslav
 *
 */
public interface IParserStack extends IParserStackLike {
	/**
	 * @return top state in the stack
	 */
	IParserState top();
	
	/**
	 * Creates a new stack by pushing a given state onto the current one
	 * @param state a state to push
	 * @return the new stack
	 */
	IParserStack push(IParserState state);
	
	/**
	 * Returns a new stack obtained from the current by removing several states from its top
	 * @param count
	 * @return the new stack
	 */
	IParserStack pop(int count);
	
}