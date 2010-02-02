package ee.stacc.productivity.edsl.sqlparser;

import java.io.IOException;

import org.jdom.JDOMException;


public class Parsers {

	public static final LRParser ARITH_PARSER;
	public static final LRParser SQL_PARSER;
	
	static {
		try {
			ARITH_PARSER = LRParser.build(Parsers.class.getClassLoader().getResource("arith.xml"));
			SQL_PARSER = LRParser.build(Parsers.class.getClassLoader().getResource("sql.xml"));
		} catch (JDOMException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
