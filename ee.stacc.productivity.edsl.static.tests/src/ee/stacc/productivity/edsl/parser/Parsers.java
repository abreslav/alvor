package ee.stacc.productivity.edsl.parser;

import java.io.IOException;

import org.jdom.JDOMException;

import ee.stacc.productivity.edsl.sqlparser.LRParser;

public class Parsers {

	public static final LRParser ARITH_PARSER;
	public static final LRParser SQL_PARSER;
	
	static {
		try {
			ARITH_PARSER = LRParser.build("../ee.stacc.productivity.edsl.sqlparser/generated/arith.xml");
			SQL_PARSER = LRParser.build("../ee.stacc.productivity.edsl.sqlparser/generated/sql.xml");
		} catch (JDOMException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
