package com.zeroturnaround.alvor.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.zeroturnaround.alvor.sqlparser.BoundedStack;
import com.zeroturnaround.alvor.sqlparser.ILRParser;
import com.zeroturnaround.alvor.sqlparser.IParserStack;
import com.zeroturnaround.alvor.sqlparser.IParserState;
import com.zeroturnaround.alvor.sqlparser.LRParser;
import com.zeroturnaround.alvor.sqlparser.Parsers;


public class ParserLoaderTest {

	@Test
	public void test() throws Exception {
		testParser(TestParsers.BIN_EXP_PARSER, new String[] {
				"SELECT NUMBER '-' NUMBER FROM ID",
		}, true);

		testParser(TestParsers.ARITH_PARSER, new String[] {
				"'1'",
				"'0'",
				"'1' '*' '0'",
				"'0' '+' '1'",
				"'1' '*' '0' '+' '1' '*' '0'",
				"'1' '+' '0' '+' '1' '*' '0'",
		}, true);
		
		testParser(TestParsers.ARITH_PARSER, new String[] {
				"'*'",
				"'+'",
				"'1' '*'",
				"'+' '1'",
				"'1' '*' '0' '1' '*' '0'",
				"'1' '+' '+' '1' '*' '0'",
		}, false);
		
		testParser(Parsers.getLALRParserForSQL(), new String[] {
				"SELECT ID FROM ID",
				"SELECT ID ',' ID FROM ID",
				"SELECT ID ',' ID FROM ID ',' ID",
				"SELECT ID FROM ID ',' ID",
				"SELECT ID ',' ID FROM ID ID",
				"SELECT ID ID FROM ID",
		}, true);
		
		testParser(Parsers.getLALRParserForSQL(), new String[] {
				"SELECT",
				"FROM",
				"SELECT FROM",
				"SELECT FROM ID",
				"SELECT ID FROM",
				"SELECT ',' ID FROM ID",
				"SELECT ID ',' FROM ID",
				"SELECT ID FROM ',' ID",
				"SELECT ID FROM ','",
		}, false);
	}

	private void testParser(ILRParser<IParserStack> sqlLalrParser, String[] correctInputs, boolean expectingAccept) {
		for (String input : correctInputs) {
			input += " $end $end";
			IParserState top = LRInterpreter.interpret((LRParser) sqlLalrParser, BoundedStack.getFactory(100, null), input, expectingAccept);
			assertEquals(top.toString(), expectingAccept, !top.isError());
		}
	}
}
