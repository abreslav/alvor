package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ee.stacc.productivity.edsl.sqlparser.SQLSyntaxChecker;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;


@RunWith(Parameterized.class)
public class SQLSyntaxCheckerTest {

	@Parameters
	public static Collection<Object[]> parameters() throws FileNotFoundException {
		List<Object[]> result = new ArrayList<Object[]>();
/*
*/		
		addFromFile("data/earved_sqls.txt", true, result);
		addFromFile("data/earved_escape.txt", true, result);
		addFromFile("data/earved_basic.txt", true, result);
		addFromFile("data/earved_bugs.txt", false, result);
		addFromFile("data/expect_fail.txt", false, result);
		
		System.out.println(result.size());
		
		return result;
	}

	private static void addFromFile(String fileName, boolean expected,
			List<Object[]> result) throws FileNotFoundException {
		List<IAbstractString> abstractStrings = AbstractStringParser.parseFile(fileName);
		
		for (IAbstractString str : abstractStrings) {
			result.add(new Object[] {str, expected});
		}
	}
	
	private final IAbstractString abstractString;
	private final boolean expected;
	
	public SQLSyntaxCheckerTest(IAbstractString abstractString, boolean expected) {
		this.abstractString = abstractString;
		this.expected = expected;
	}

	@Test
	public void testSQL() throws Exception {
		List<String> errors = SQLSyntaxChecker.INSTANCE.check(abstractString);
		assertEquals(errors + "   " + abstractString.toString(), expected, errors.isEmpty());
	}
	
//	@Test
//	public void test() throws Exception {
//		assertFalse(check("\" 1asd\""));
//		assertFalse(check("\" 'sadfasf\""));
//		assertFalse(check("\" №\""));
//		assertTrue(check("\"SELECT a FROM b\""));
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void testLoopChecker() throws Exception {
//		check("(\"a\")+");
//		fail();
//	}
//	
//	private boolean check(String astr) {
//		return SQLSyntaxChecker.INSTANCE.check(AbstractStringParser.parseOneFromString(astr)).isEmpty();
//	}
}
