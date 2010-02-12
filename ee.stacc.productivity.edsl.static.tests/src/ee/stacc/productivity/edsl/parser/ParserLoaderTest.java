package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;
import ee.stacc.productivity.edsl.sqlparser.Parsers;
import ee.stacc.productivity.edsl.sqlparser.SimpleLinkedStack;


public class ParserLoaderTest {

	@Test
	public void test() throws Exception {
		testParser(Parsers.BIN_EXP_PARSER, new String[] {
				"SELECT NUMBER '-' NUMBER FROM ID",
		}, true);

		testParser(Parsers.ARITH_PARSER, new String[] {
				"'1'",
				"'0'",
				"'1' '*' '0'",
				"'0' '+' '1'",
				"'1' '*' '0' '+' '1' '*' '0'",
				"'1' '+' '0' '+' '1' '*' '0'",
		}, true);
		
		testParser(Parsers.ARITH_PARSER, new String[] {
				"'*'",
				"'+'",
				"'1' '*'",
				"'+' '1'",
				"'1' '*' '0' '1' '*' '0'",
				"'1' '+' '+' '1' '*' '0'",
		}, false);
		
		testParser(Parsers.SQL_PARSER, new String[] {
				"SELECT ID FROM ID",
				"SELECT ID ',' ID FROM ID",
				"SELECT ID ',' ID FROM ID ',' ID",
				"SELECT ID FROM ID ',' ID",
				"SELECT ID ',' ID FROM ID ID",
				"SELECT ID ID FROM ID",
		}, true);
		
		testParser(Parsers.SQL_PARSER, new String[] {
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

	private void testParser(LRParser parser, String[] correctInputs, boolean expectingAccept) {
		for (String input : correctInputs) {
			input += " $end $end";
			IParserState top = LRInterpreter.interpret(parser, SimpleLinkedStack.FACTORY, input, expectingAccept);
			assertEquals(top.toString(), expectingAccept, !top.isError());
		}
	}
}
