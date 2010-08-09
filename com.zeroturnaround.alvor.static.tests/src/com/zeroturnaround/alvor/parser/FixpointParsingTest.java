package com.zeroturnaround.alvor.parser;

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

import com.zeroturnaround.alvor.lexer.automata.AutomataParser;
import com.zeroturnaround.alvor.lexer.automata.IAlphabetConverter;
import com.zeroturnaround.alvor.lexer.automata.State;
import com.zeroturnaround.alvor.sqlparser.BoundedStack;
import com.zeroturnaround.alvor.sqlparser.ILRParser;
import com.zeroturnaround.alvor.sqlparser.IParserStack;
import com.zeroturnaround.alvor.sqlparser.IStackFactory;
import com.zeroturnaround.alvor.sqlparser.ParserSimulator;
import com.zeroturnaround.alvor.sqlparser.Parsers;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.parser.AbstractStringParser;

@RunWith(Parameterized.class)
public class FixpointParsingTest {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] {
//				{SimpleStack.FACTORY},
//				{SimpleLinkedStack.FACTORY},
//				{SimpleFoldedStack.FACTORY},
				{BoundedStack.getFactory(100, null)},
		});
	}

	private final IStackFactory<IParserStack> stackFactory;
	
	public FixpointParsingTest(IStackFactory<IParserStack> stackFactory) {
		this.stackFactory = stackFactory;
	}
	
	private static ILRParser<IParserStack> parser = Parsers.getLALRParserForSQL();
	
	@Test
	public void testSimple() throws Exception {
		State initial;
		
		initial = AutomataParser.parse("A - B:S; B - C:I; C - D:F; D - E:I; E - !X:X");
		assertTrue(doParse(parser, initial));
		
		initial = AutomataParser.parse("A - B:S; B - C:S; C - D:F; D - E:I; E - !X:X");
		assertFalse(doParse(parser, initial));
	}

	private boolean doParse(final ILRParser<IParserStack> parser, State initial) {
//		return SQLSyntaxChecker.INSTANCE.parseAutomaton(initial, new IAlphabetConverter() {
		return ParserSimulator.getLALRInstance().parseAutomaton(initial, new IAlphabetConverter() {
			
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
		});//, stackFactory);
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

		
		abstractString = "\"a\" \"SELECT * FROM t\"";
		assertFalse(parseAbstractString(abstractString));
		
		
		abstractString = "\"\" \"a\" \"SELECT * FROM t\"";
		assertFalse(parseAbstractString(abstractString));
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
		
		String abstractString;

		abstractString = "\"SELECT asd\" (\", dsd \")+ \"FROM asd, sdf\"";
		assertTrue(parseAbstractString(abstractString));
				
		abstractString = "\"SELECT asd\" (\", dsd \")+ \"IN FROM asd, sdf\"";
		assertFalse(parseAbstractString(abstractString));
		
		abstractString = "\"SELECT asd\" (\", dsd \")+ \", FROM asd, sdf\"";
		assertFalse(parseAbstractString(abstractString));
		
		abstractString = "\"SELECT asd\" (\", , dsd \")+ \" FROM asd, sdf\"";
		assertFalse(parseAbstractString(abstractString));
		
	}
	
	private void assertParses(String abstractString) {
		IAbstractString as = AbstractStringParser.parseOneFromString(abstractString);
//		List<String> errors = SQLSyntaxChecker.INSTANCE.checkAbstractString(as, stackFactory);
		List<String> errors = ParserSimulator.getGLRInstance().check(as);//, stackFactory);
		assertTrue(errors.toString(), errors.isEmpty());
	}

	private boolean parseAbstractString(String abstractString) {
		IAbstractString as = AbstractStringParser.parseOneFromString(abstractString);
//		return SQLSyntaxChecker.INSTANCE.checkAbstractString(as, stackFactory).isEmpty();
		return ParserSimulator.getGLRInstance().check(as).isEmpty();//, stackFactory).isEmpty();
	}
}
