package ee.stacc.productivity.edsl.string.parser;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import ee.stacc.productivity.edsl.string.IAbstractString;


public class AbstractStringParserTest {

	@Test
	public void testLexer() throws Exception {
		AbstractStringLexer lexer = new AbstractStringLexer(new StringReader("\"\\\"asdas\" {[fdsa\\][], (\"\")+} "));
		StringBuilder res = new StringBuilder();
		while (true) {
			Token token = lexer.yylex();
			res.append(token).append(" ");
			if (token == Token.EOF) {
				break;
			}
		}
		assertEquals("CONSTANT(\"asdas) OPEN_CURLY CHAR_SET(fdsa][) COMMA OPEN_ITER CONSTANT() CLOSE_ITER CLOSE_CURLY EOF ", res.toString());
	}
	
	@Test
	public void testParser() throws Exception {
		
		parserTest("\"\\\"asdas\" ");
		parserTest("[fdsa\\][] ");
		parserTest("([fdsa\\][] )+ ");
		parserTest("{[fdsa\\][] , \"\" } ");
		
		AbstractStringLexer lexer = new AbstractStringLexer(new StringReader(
				"\"\\\"asdas\" {[fdsa\\][] , (\"\" )+ } \n\"\" \n\n [] \n"
		));
		
		AbstractStringParser parser = new AbstractStringParser(lexer);
		List<IAbstractString> strings = parser.strings();
		assertEquals("[\"\\\"asdas\" {[fdsa\\][] , (\"\" )+ } , \"\" , [] ]", strings.toString());
	}
	
	@Test
	public void testParseFile() throws Exception {
		AbstractStringParser.parseFile("data/sqls.txt");
		AbstractStringParser.parseFile("data/big.txt");
	}

	private void parserTest(String input) {
		AbstractStringLexer lexer = new AbstractStringLexer(new StringReader(input));
		AbstractStringParser parser = new AbstractStringParser(lexer);
		IAbstractString string = parser.abstractString();
		assertEquals(input, string.toString());
	}
}
