package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.SQLSyntaxChecker;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;


public class SQLSyntaxCheckerTest {

	@Test
	public void test() throws Exception {
		assertFalse(check("\" 1asd\""));
		assertFalse(check("\" 'sadfasf\""));
		assertFalse(check("\" №\""));
		assertTrue(check("\"SELECT a FROM b\""));
	}

	private boolean check(String astr) {
		return SQLSyntaxChecker.INSTANCE.check(AbstractStringParser.parseOneFromString(astr)).isEmpty();
	}
}
