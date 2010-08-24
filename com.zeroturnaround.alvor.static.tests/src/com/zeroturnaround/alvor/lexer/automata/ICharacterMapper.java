/**
 * 
 */
package ee.stacc.productivity.edsl.lexer.automata;

public interface ICharacterMapper extends IInputToString {
	String map(int c);
}