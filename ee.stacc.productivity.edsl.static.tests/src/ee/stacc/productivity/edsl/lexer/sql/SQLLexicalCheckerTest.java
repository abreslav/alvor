package ee.stacc.productivity.edsl.lexer.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;

@RunWith(Parameterized.class)
public class SQLLexicalCheckerTest {

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
	
	public SQLLexicalCheckerTest(IAbstractString abstractString) {
		this.abstractString = abstractString;
	}

	@Test
	public void testLexer() throws Exception {
		List<String> errors = SQLLexicalChecker.INSTANCE.check(abstractString);
		assertTrue(errors + "   "  + abstractString, errors.isEmpty());
	}

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
