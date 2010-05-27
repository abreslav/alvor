package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jdom.JDOMException;
import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.GLRParser;
import ee.stacc.productivity.edsl.sqlparser.GLRStack;
import ee.stacc.productivity.edsl.sqlparser.ILRParser;
import ee.stacc.productivity.edsl.sqlparser.ParserSimulator;
import ee.stacc.productivity.edsl.string.StringConstant;


public class GLRParserTest {

	private static final ParserSimulator<GLRStack> GLR_INSTANCE = new ParserSimulator<GLRStack>(loadParser(), GLRStack.FACTORY);

	private static ILRParser<GLRStack> loadParser() {
		try {
			return GLRParser.build(GLRParserTest.class.getClassLoader().getResource("glr.xml"));
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		} catch (JDOMException e) {
			throw new AssertionError(e);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	@Test
	public void test() throws Exception {
		String text;

		text = "1 ";
		assertFalse(parse(new StringConstant(text)));
		text = "1 . 2 ";
		assertTrue(parse(new StringConstant(text)));
		text = "1 + 1 . 2";
		assertFalse(parse(new StringConstant(text)));
		text = "1 . 2 +  ";
		assertFalse(parse(new StringConstant(text)));
		text = "1 . 2 + 1";
		assertFalse(parse(new StringConstant(text)));
		text = "+ 1 . 2 ";
		assertFalse(parse(new StringConstant(text)));
		text = "1 . 2 + 1 . 2 ";
		assertTrue(parse(new StringConstant(text)));
	}

	private boolean parse(StringConstant str) {
		return GLR_INSTANCE.check(str).isEmpty();
	}
}
