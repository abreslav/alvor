package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.*;

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
		List<IAbstractString> abstractStrings = AbstractStringParser.parseFile("data/earved_sqls.txt");
		
		List<Object[]> result = new ArrayList<Object[]>();
		for (IAbstractString str : abstractStrings) {
			result.add(new Object[] {str});
		}
		return result;
	}
	
	private final IAbstractString abstractString;
	
	public SQLSyntaxCheckerTest(IAbstractString abstractString) {
		this.abstractString = abstractString;
	}

	@Test
	public void testSQL() throws Exception {
		List<String> errors = SQLSyntaxChecker.INSTANCE.check(abstractString);
		assertTrue(errors + "   " + abstractString.toString(), errors.isEmpty());
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
	private boolean check(String astr) {
		return SQLSyntaxChecker.INSTANCE.check(AbstractStringParser.parseOneFromString(astr)).isEmpty();
	}
}
