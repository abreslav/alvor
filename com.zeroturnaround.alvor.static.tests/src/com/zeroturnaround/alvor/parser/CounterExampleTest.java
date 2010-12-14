package com.zeroturnaround.alvor.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.zeroturnaround.alvor.sqlparser.ILRParser;
import com.zeroturnaround.alvor.sqlparser.IParserStack;
import com.zeroturnaround.alvor.sqlparser.ParserSimulator;
import com.zeroturnaround.alvor.sqlparser.Parsers;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.parser.AbstractStringParser;

public class CounterExampleTest {
	
	private static ILRParser<IParserStack> parser = Parsers.getLALRParserForSQL();
	
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
		List<String> errors = ParserSimulator.getGLRInstance().check(as);
		assertTrue(errors.toString(), errors.isEmpty());
	}

	private boolean parseAbstractString(String abstractString) {
		IAbstractString as = AbstractStringParser.parseOneFromString(abstractString);
		return ParserSimulator.getGLRInstance().check(as).isEmpty();
	}
}
