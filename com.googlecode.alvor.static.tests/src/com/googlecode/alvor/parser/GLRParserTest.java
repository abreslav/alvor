package com.googlecode.alvor.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.googlecode.alvor.sqllexer.GenericSQLLexerData;
import com.googlecode.alvor.sqlparser.GLRParser;
import com.googlecode.alvor.sqlparser.GLRStack;
import com.googlecode.alvor.sqlparser.ILRParser;
import com.googlecode.alvor.sqlparser.ParserSimulator;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.parser.AbstractStringParser;


public class GLRParserTest {

	private static final ParserSimulator<GLRStack> GENERIC_GLR_INSTANCE = new ParserSimulator<GLRStack>(loadParser(), 
			GLRStack.FACTORY, GenericSQLLexerData.DATA);

	private static ILRParser<GLRStack> loadParser() {
		return GLRParser.build(GLRParserTest.class.getClassLoader().getResource("glr.xml"));
	}
	
	@Test
	public void test() throws Exception {
		String text;

		text = "\"1\" (\"+ 1\")+";
		assertParses(AbstractStringParser.parseOneFromString(text));

		text = "\"1\" (\"* 1\")+";
		assertParses(AbstractStringParser.parseOneFromString(text));
		
		text = "1 ";
		assertParses(new StringConstant(text));
		
		text = "1 . 2 ";
		assertParsesNot(new StringConstant(text));
		text = "1 . 2 + 1 . 2 ";
		assertParsesNot(new StringConstant(text));
		text = "1 + 1 . 2";
		assertParsesNot(new StringConstant(text));
		text = "1 . 2 +  ";
		assertParsesNot(new StringConstant(text));
		text = "1 . 2 + 1";
		assertParsesNot(new StringConstant(text));
		text = "+ 1 . 2 ";
		assertParsesNot(new StringConstant(text));
	}

	private void assertParses(IAbstractString str) {
		List<String> check = GENERIC_GLR_INSTANCE.check(str);
		assertTrue(check.toString(), check.isEmpty());
	}

	private void assertParsesNot(IAbstractString str) {
		List<String> check = GENERIC_GLR_INSTANCE.check(str);
		assertFalse(check.isEmpty());
	}
}
