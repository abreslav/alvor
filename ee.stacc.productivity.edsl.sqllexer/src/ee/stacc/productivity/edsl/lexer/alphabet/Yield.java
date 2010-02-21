package ee.stacc.productivity.edsl.lexer.alphabet;

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
