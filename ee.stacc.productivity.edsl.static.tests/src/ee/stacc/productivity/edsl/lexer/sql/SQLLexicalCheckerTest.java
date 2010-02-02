package ee.stacc.productivity.edsl.lexer.sql;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;


public class SQLLexicalCheckerTest {

	@Test
	public void test() throws Exception {
		SQLLexicalChecker checker = SQLLexicalChecker.INSTANCE;
		IAbstractString string = AbstractStringParser.parseOneFromString("\"12a &\"");
		List<String> result = checker.check(string);
		assertEquals(
				new LinkedHashSet<String>(Arrays.asList(new String[] {
						"Erroneous token: DIGAL_ERR",
						"Erroneous token: UNKNOWN_CHARACTER_ERR",
				})),
				new LinkedHashSet<String>(result)
		);
	}
}
