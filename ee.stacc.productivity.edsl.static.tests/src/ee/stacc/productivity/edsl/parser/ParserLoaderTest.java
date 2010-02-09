package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertSame;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;
import ee.stacc.productivity.edsl.sqlparser.Parsers;
import ee.stacc.productivity.edsl.sqlparser.SimpleStack;


public class ParserLoaderTest {

	@Test
	public void test() throws Exception {
		testParser(Parsers.ARITH_PARSER, new String[] {
				"'1'",
				"'0'",
				"'1' '*' '0'",
				"'0' '+' '1'",
				"'1' '*' '0' '+' '1' '*' '0'",
				"'1' '+' '0' '+' '1' '*' '0'",
		}, IParserState.ACCEPT);

		testParser(Parsers.ARITH_PARSER, new String[] {
				"'*'",
				"'+'",
				"'1' '*'",
				"'+' '1'",
				"'1' '*' '0' '1' '*' '0'",
				"'1' '+' '+' '1' '*' '0'",
		}, IParserState.ERROR);
		
		testParser(Parsers.SQL_PARSER, new String[] {
				"SELECT ID FROM ID",
				"SELECT ID ',' ID FROM ID",
				"SELECT ID ',' ID FROM ID ',' ID",
				"SELECT ID FROM ID ',' ID",
				"SELECT ID ',' ID FROM ID ID",
		}, IParserState.ACCEPT);
		
		testParser(Parsers.SQL_PARSER, new String[] {
				"SELECT",
				"FROM",
				"SELECT FROM",
				"SELECT FROM ID",
				"SELECT ID FROM",
				"SELECT ID ID FROM ID",
				"SELECT ',' ID FROM ID",
				"SELECT ID ',' FROM ID",
				"SELECT ID FROM ',' ID",
				"SELECT ID FROM ','",
		}, IParserState.ERROR);
	}

	private void testParser(LRParser parser, String[] correctInputs, IParserState expectedState) {
		for (String input : correctInputs) {
			input += " $end $end";
			IParserState top = LRInterpreter.interpret(parser, SimpleStack.FACTORY, input, expectedState);
			assertSame(expectedState, top);
		}
	}
}
