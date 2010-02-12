package ee.stacc.productivity.edsl.sqlparser;

import java.io.IOException;

import org.jdom.JDOMException;


public class Parsers {

	public static final LRParser SQL_PARSER;
	
	static {
		try {
			SQL_PARSER = LRParser.build(Parsers.class.getClassLoader().getResource("ee/stacc/productivity/edsl/sqlparser/sql.xml"));
		} catch (JDOMException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
