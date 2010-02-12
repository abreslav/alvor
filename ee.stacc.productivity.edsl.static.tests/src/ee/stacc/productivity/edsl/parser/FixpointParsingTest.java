package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ee.stacc.productivity.edsl.lexer.automata.AutomataParser;
import ee.stacc.productivity.edsl.lexer.automata.IAlphabetConverter;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.sqlparser.IStackFactory;
import ee.stacc.productivity.edsl.sqlparser.LRParser;
import ee.stacc.productivity.edsl.sqlparser.Parsers;
import ee.stacc.productivity.edsl.sqlparser.SQLSyntaxChecker;
import ee.stacc.productivity.edsl.sqlparser.SimpleLinkedStack;
import ee.stacc.productivity.edsl.sqlparser.SimpleStack;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;

@RunWith(Parameterized.class)
public class FixpointParsingTest {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] {
				{SimpleStack.FACTORY},
				{SimpleLinkedStack.FACTORY},
				{SimpleFoldedStack.FACTORY},
		});
	}

	private final IStackFactory stackFactory;
	
	public FixpointParsingTest(IStackFactory stackFactory) {
		this.stackFactory = stackFactory;
	}
	
	private static LRParser parser = Parsers.SQL_PARSER;
	
	@Test
	public void testSimple() throws Exception {
		State initial;
		
		initial = AutomataParser.parse("A - B:S; B - C:I; C - D:F; D - E:I; E - !X:X");
		assertTrue(doParse(parser, initial));
		
		initial = AutomataParser.parse("A - B:S; B - C:S; C - D:F; D - E:I; E - !X:X");
		assertFalse(doParse(parser, initial));
	}

	private boolean doParse(final LRParser parser, State initial) {
		return SQLSyntaxChecker.INSTANCE.parseAutomaton(initial, new IAlphabetConverter() {
			
			@Override
			public int convert(int c) {
				Map<String, Integer> namesToTokenNumbers = parser.getNamesToTokenNumbers();
				switch (c) {
				case 'S':
					return namesToTokenNumbers.get("SELECT");
				case 'I':
					return namesToTokenNumbers.get("ID");
				case 'F':
					return namesToTokenNumbers.get("FROM");
				case 'X':
					return namesToTokenNumbers.get("$end");
				case ',':
					return namesToTokenNumbers.get("','");
				}
				throw new IllegalStateException("Unknown token: " + c);
			}
		}, stackFactory);
	}
	
	@Test
	public void testSQL() throws Exception {
		String abstractString;

		abstractString = "\"SELECT sd.x, asd FROM asd\"";
		assertParses(abstractString);
		
		
		abstractString = "\"SELECT sd, asd FROM asd\"";
		assertParses(abstractString);
		
		
		abstractString = "\"SELECT asd FROM asd, dsd\"";
		assertTrue(parseAbstractString(abstractString));

		
		abstractString = "\"SELECT asd, dsd FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd FROM asd\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd asd FROM asd\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"asd FROM asd\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd FROM asd,\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd FROM ,\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd\" (\", dsd\")+ \"FROM asd, sdf\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd\" {\", dsd\", \"\"} \" FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd\" {\", dsd\", \"\", \", a, s, v\"} \" FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
	}

	@Test
	public void testNestedParentheses() throws Exception {
		String abstractString;
		
		abstractString = "\"SELECT a(*, a(x)) FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT a(a(*), a(b, c(d(e)))) FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
	}
	
	@Test
	public void testLoops() throws Exception {
		if (stackFactory == SimpleStack.FACTORY) {
			return;
		}
		
		String abstractString;

		abstractString = "\"SELECT asd\" (\", dsd \")+ \"FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
		
		
		abstractString = "\"SELECT asd\" (\", dsd \")+ \"IN FROM asd, sdf\"";
		assertFalse(parseAbstractString(abstractString));
		
	}
	
	private void assertParses(String abstractString) {
		IAbstractString as = AbstractStringParser.parseOneFromString(abstractString);
		List<String> errors = SQLSyntaxChecker.INSTANCE.checkAbstractString(as, stackFactory);
		assertTrue(errors.toString(), errors.isEmpty());
	}

	private boolean parseAbstractString(String abstractString) {
		IAbstractString as = AbstractStringParser.parseOneFromString(abstractString);
		return SQLSyntaxChecker.INSTANCE.checkAbstractString(as, stackFactory).isEmpty();
	}
}
