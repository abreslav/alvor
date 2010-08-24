package ee.stacc.productivity.edsl.string.parser;

public class Token {
	public static final Token OPEN_CURLY = new Token(TokenType.OPEN_CURLY);
	public static final Token CLOSE_CURLY = new Token(TokenType.CLOSE_CURLY);
	public static final Token COMMA = new Token(TokenType.COMMA);
	public static final Token OPEN_ITER = new Token(TokenType.OPEN_ITER);
	public static final Token CLOSE_ITER = new Token(TokenType.CLOSE_ITER);
	public static final Token EOF = new Token(TokenType.EOF);
	public static final Token NEWLINE = new Token(TokenType.NEWLINE);

	public static Token newConstant(String s) {
		return new Token(TokenType.CONSTANT, clearEscapes(s));
	}
	
	public static Token newCharSet(String s) {
		return new Token(TokenType.CHAR_SET, clearEscapes(s));
	}
	
	private static String clearEscapes(String s) {
		StringBuilder result = new StringBuilder();
		boolean escape = false;
		for (int i = 1; i < s.length() - 1; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\\':
				escape = !escape;
				if (escape) {
					break;
				}
				/* FALL THROUGH */
			default:
				result.append(c);
				escape = false;
			}
		}
		return result.toString();
	}

	private final TokenType type;
	private final String text;
	
	private Token(TokenType type) {
		this(type, "");
	}

	private Token(TokenType type, String string) {
		this.type = type;
		this.text = string;
	}

	public TokenType getType() {
		return type;
	}

	public String getText() {
		return text;
	}
	
	@Override
	public String toString() {
		return type + (text == "" ? "" : "(" + text + ")");
	}
}
