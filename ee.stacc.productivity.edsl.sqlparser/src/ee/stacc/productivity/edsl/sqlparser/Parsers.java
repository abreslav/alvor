package ee.stacc.productivity.edsl.sqlparser;

import java.io.IOException;

import org.jdom.JDOMException;


public class Parsers {

	public static final LRParser ARITH_PARSER;
	public static final LRParser BIN_EXP_PARSER;
	public static final LRParser SQL_PARSER;
	
	static {
		try {
			BIN_EXP_PARSER = LRParser.build(Parsers.class.getClassLoader().getResource("binexp.xml"));
			ARITH_PARSER = LRParser.build(Parsers.class.getClassLoader().getResource("arith.xml"));
			SQL_PARSER = LRParser.build(Parsers.class.getClassLoader().getResource("sql.xml"));
		} catch (JDOMException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
