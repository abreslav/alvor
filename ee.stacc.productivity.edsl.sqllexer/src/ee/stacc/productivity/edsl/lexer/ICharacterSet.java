/**
 * 
 */
package ee.stacc.productivity.edsl.lexer;

import java.util.Set;


public interface ICharacterSet {
	boolean contains(char c);
	boolean containsSet(ICharacterSet other);
	boolean intersects(ICharacterSet other);
	ICharacterSet join(ICharacterSet other);
	Set<Character> asJavaSet();
	boolean isEmpty();
}