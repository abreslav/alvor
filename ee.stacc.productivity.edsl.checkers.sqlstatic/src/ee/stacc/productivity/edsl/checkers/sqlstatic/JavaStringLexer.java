package ee.stacc.productivity.edsl.checkers.sqlstatic;

import ee.stacc.productivity.edsl.checkers.IPositionDescriptor;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public class JavaStringLexer {
	private static enum LexerState {
		EXPECT_STRING_START,
		FIRST_NORMAL,
		SECOND_NORMAL,
		BACKSLASH,
		BACKSLASH_U,
		BACKSLASH_DIGIT,
		EXPECT_CHAR_START,
//		EXPECT_CHAR_STOP,
		OK;
	}
	
	public static void tokenizeJavaString(String escapedValue,
			IAbstractInputItem[] result, IPositionDescriptor stringPosition) {
		if (escapedValue == null) {
			throw new MalformedStringLiteralException("Literal value is null");
		}
		boolean isCharacter = escapedValue.startsWith("'");
		LexerState state = isCharacter ? LexerState.EXPECT_CHAR_START : LexerState.EXPECT_STRING_START;
		
		int currentResultPosition = 0;
		int symbolStart = -1;
		for (int i = 0; i < escapedValue.length(); i++) {
			char c = escapedValue.charAt(i);
			switch (state) {
			case EXPECT_CHAR_START:
				if (c == '\'') {
					state = LexerState.FIRST_NORMAL;
				} else {
					throw new MalformedStringLiteralException("Character must start with a single quote");
				}
				break;
			case EXPECT_STRING_START:
				if (c == '\"') {
					state = LexerState.FIRST_NORMAL;
				} else {
					throw new MalformedStringLiteralException("String must start with a double quote");
				}
				break;
			case SECOND_NORMAL:
				if (isCharacter) {
					if (c == '\'') {
						state = LexerState.OK;
						break;
					} else {
						throw new MalformedStringLiteralException("Character literal sould have ended");
					}
				}
				/* FALL THROUGH */
			case FIRST_NORMAL:
				switch (c) {
				case '\"':
					if (isCharacter) {
						result[currentResultPosition] = new PositionedCharacter(c, stringPosition, i, 1);
						currentResultPosition++;
						state = LexerState.SECOND_NORMAL;
					} else {
						state = LexerState.OK;
					}
					break;
				case '\\':
					state = LexerState.BACKSLASH;
					break;
				case '\n':
				case '\r':
					throw new MalformedStringLiteralException("Illegal character: code = " + (int) c);
				default:
					result[currentResultPosition] = new PositionedCharacter(c, stringPosition, i, 1);
					currentResultPosition++;
					state = LexerState.SECOND_NORMAL;
					break;
				}
				break;
			case BACKSLASH:
				switch (c) {
				case 'b':
				case 't':
				case 'n':
				case 'f':
				case 'r':
				case '\"':
				case '\'':
				case '\\':
					result[currentResultPosition] = new PositionedCharacter(escapeToChar(c), stringPosition, i - 1, 2);
					currentResultPosition++;
					state = LexerState.SECOND_NORMAL;
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					symbolStart = i - 1;
					state = LexerState.BACKSLASH_DIGIT;
					break;
				case 'u':
					symbolStart = i - 1;
					state = LexerState.BACKSLASH_U;
					break;
				default:
					throw new MalformedStringLiteralException("Illegal escape: \\" + c);
				}
				break;
			case BACKSLASH_DIGIT:
				if (i > symbolStart + 3 || !Character.isDigit(c)) {
					result[currentResultPosition] = new PositionedCharacter(
							octToChar(escapedValue.substring(symbolStart + 1, i)), 
							stringPosition, 
							symbolStart, i - symbolStart);
					currentResultPosition++;
					symbolStart = -1;
					state = LexerState.SECOND_NORMAL;
					i--;
				} else {
					// Just go on
				}
				break;					
			case BACKSLASH_U:
				if (i > symbolStart + 5) {
					result[currentResultPosition] = new PositionedCharacter(
							(Character) hexToChar(escapedValue.substring(symbolStart + 2, i)), 
							stringPosition, 
							symbolStart, i - symbolStart);
					currentResultPosition++;
					symbolStart = -1;
					state = LexerState.SECOND_NORMAL;
					i--;
				} else {
					// Just go on
				}
				break;
			case OK:
				throw new MalformedStringLiteralException("Text after closing quote");
			}
		}
		if (state != LexerState.OK) {
			throw new MalformedStringLiteralException("Unfinished literal");
		}
		assert currentResultPosition == result.length;
	}
	
	private static char hexToChar(String s) {
		char result = 0;
		int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			result = (char) (result * 16);
			if ('a' <= c && c <= 'f') {
				result += c - 'a' + 10;
			} else if ('A' <= c && c <= 'F') {
				result += c - 'A' + 10;
			} else if ('0' <= c && c <= '9') {
				result += c - '0';
			} else {
				throw new MalformedStringLiteralException("Malformed unicode escape");
			}
		}
		return result;
	}

	private static char octToChar(String substring) {
		assert substring.length() <= 3;
		assert substring.length() > 0;
		if (substring.length() == 3 && substring.charAt(0) > '3') {
			throw new MalformedStringLiteralException("Malformed octal escape");
		}
		char c = 0;
		int length = substring.length();
		for (int i = 0; i < length; i++) {
			c = (char) (c * 8 + substring.charAt(i) - '0');
		}
		return c;
	}

	private static char escapeToChar(char c) {
		switch (c) {
		case 'b':
			return '\b';
		case 't':
			return '\t';
		case 'n':
			return '\n';
		case 'f':
			return '\f';
		case 'r':
			return '\r';
		case '\"':
		case '\'':
		case '\\':
			return  c;
		default:
			throw new MalformedStringLiteralException("Incorrect escape char: " + c);
		}
	}
	

}
