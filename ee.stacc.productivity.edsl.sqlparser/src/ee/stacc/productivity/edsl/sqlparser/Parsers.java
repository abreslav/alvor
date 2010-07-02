package ee.stacc.productivity.edsl.sqlparser;

import java.io.IOException;

import org.jdom.JDOMException;

/**
 * This class stores parsers loaded from parsing tables (produced by Bison and stored in XML) 
 * 
 * @author abreslav
 *
 */
public class Parsers {

	/**
	 * LR-parser for a simple grammar. In fact, it's not needed, kept just in case
	 */
	public static final LRParser SQL_PARSER;
	
	/**
	 * GLR-parser for a reacher grammar
	 */
	public static final GLRParser SQL_GLR_PARSER;
	
	static {
		try {
			SQL_PARSER = LRParser.build(Parsers.class.getClassLoader().getResource("ee/stacc/productivity/edsl/sqlparser/sql.lr.xml"));
			SQL_GLR_PARSER = GLRParser.build(Parsers.class.getClassLoader().getResource("ee/stacc/productivity/edsl/sqlparser/sql.xml"));
		} catch (JDOMException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
