package com.zeroturnaround.alvor.checkers.sqlstatic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;


public class JavaStringLexerTest {

	@Test
	public void test() throws Exception {
		String string;
		
		string = "\"\"";
		check(string);
		
		string = "\"abc\"";
		check(string,
				new PositionedCharacter('a', null, 1, 1),
				new PositionedCharacter('b', null, 2, 1),
				new PositionedCharacter('c', null, 3, 1)
		);
		
		string = "\"\\\"\"";
		check(string,
				new PositionedCharacter('"', null, 1, 2)
		);
		
		string = "\"'\"";
		check(string,
				new PositionedCharacter('\'', null, 1, 1)
		);
		
		string = "\"abc \\b \\f\\n\"";
		check(string,
				new PositionedCharacter('a', null, 1, 1),
				new PositionedCharacter('b', null, 2, 1),
				new PositionedCharacter('c', null, 3, 1),
				new PositionedCharacter(' ', null, 4, 1),
				new PositionedCharacter('\b', null, 5, 2),
				new PositionedCharacter(' ', null, 7, 1),
				new PositionedCharacter('\f', null, 8, 2),
				new PositionedCharacter('\n', null, 10, 2)
		);
		
		string = "\"abc\\0 \\01 \\001 \\002\"";
		check(string,
				new PositionedCharacter('a', null, 1, 1),
				new PositionedCharacter('b', null, 2, 1),
				new PositionedCharacter('c', null, 3, 1),
				new PositionedCharacter(0, null, 4, 2),
				new PositionedCharacter(' ', null, 6, 1),
				new PositionedCharacter(1, null, 7, 3),
				new PositionedCharacter(' ', null, 10, 1),
				new PositionedCharacter(1, null, 11, 4),
				new PositionedCharacter(' ', null, 15, 1),
				new PositionedCharacter(2, null, 16, 4)
		);
		
		string = "\"abc\\u0001\"";
		check(string,
				new PositionedCharacter('a', null, 1, 1),
				new PositionedCharacter('b', null, 2, 1),
				new PositionedCharacter('c', null, 3, 1),
				new PositionedCharacter(1, null, 4, 6)
		);
		
		string = "'a'";
		check(string,
				new PositionedCharacter('a', null, 1, 1)
		);
		
		string = "'\"'";
		check(string,
				new PositionedCharacter('"', null, 1, 1)
		);
		
		string = "'\\1'";
		check(string,
				new PositionedCharacter(1, null, 1, 2)
		);
		
		string = "'\\01'";
		check(string,
				new PositionedCharacter(1, null, 1, 3)
		);
		
		string = "'\\001'";
		check(string,
				new PositionedCharacter(1, null, 1, 4)
		);
		
		string = "'\\u0001'";
		check(string,
				new PositionedCharacter(1, null, 1, 6)
		);
		
		string = "'\\n'";
		check(string,
				new PositionedCharacter('\n', null, 1, 2)
		);
	}
	
	private void check(String string, IAbstractInputItem... expected) {
		IAbstractInputItem[] result = new IAbstractInputItem[expected.length];
		JavaStringLexer.tokenizeJavaString(string, result, null);
		for (int i = 0; i < result.length; i++) {
			String message = (result[i] + " != " + expected[i]).replaceAll("\u0000", "\\0");
			assertEquals(message, expected[i], result[i]);
		}
	}
	
}
