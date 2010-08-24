package ee.stacc.productivity.edsl.parser;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import ee.stacc.productivity.edsl.sqlparser.LRParser;

public class TestParsers {

	public static final LRParser ARITH_PARSER;
	public static final LRParser BIN_EXP_PARSER;
	
	static {
		try {
			BIN_EXP_PARSER = LRParser.build(new File("data/binexp.xml").toURI().toURL());
			ARITH_PARSER = LRParser.build(new File("data/arith.xml").toURI().toURL());
		} catch (JDOMException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
