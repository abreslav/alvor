package ee.stacc.productivity.edsl.lexer.alphabet;

import ee.stacc.productivity.edsl.lexer.automata.IOutputItemInterpreter;

/**
 * An "output command" for transducers: tells the interpreter ({@link IOutputItemInterpreter}) to 
 * create a new token with and put in all the text remembererd in the buffer (see {@link PushInput}).
 *  
 * @author abreslav
 *
 */
public class Yield implements IAbstractOutputItem {
	
	public static Yield YIELD_EOF = create(-1);
	
	public static Yield create(int tokenType) {
		return new Yield(tokenType);
	}
	
	private final int tokenType;

	private Yield(int tokenType) {
		this.tokenType = tokenType;
	}
	
	public int getTokenType() {
		return tokenType;
	}
}
