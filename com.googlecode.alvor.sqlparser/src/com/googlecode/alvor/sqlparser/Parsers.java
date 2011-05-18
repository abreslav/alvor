package com.googlecode.alvor.sqlparser;

import java.io.IOException;

import org.jdom.JDOMException;

/**
 * This class stores parsers loaded from parsing tables (produced by Bison and stored in XML) 
 * 
 * @author abreslav
 *
 */
public class Parsers {

	private static ILRParser<IParserStack> SQL_LALR_PARSER;
	
	private static ILRParser<GLRStack> SQL_GLR_PARSER;
	
	/**
	 * LR-parser for a simple grammar. In fact, it's not needed, kept just in case
	 */
	public static ILRParser<IParserStack> getLALRParserForSQL() {
		if (SQL_LALR_PARSER == null) {
			try {
				SQL_LALR_PARSER = LRParser.build(Parsers.class.getClassLoader().getResource("com/googlecode/alvor/sqlparser/sql.lr.xml"));
			} catch (JDOMException e) {
				throw new IllegalStateException(e);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return SQL_LALR_PARSER;
	}

	/**
	 * GLR-parser for a reacher grammar
	 */
	public static ILRParser<GLRStack> getGLRParserForSQL() {
		if (SQL_GLR_PARSER == null) {
			try {
				SQL_GLR_PARSER = GLRParser.build(Parsers.class.getClassLoader().getResource("com/googlecode/alvor/sqlparser/sql.xml"));
			} catch (JDOMException e) {
				throw new IllegalStateException(e);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return SQL_GLR_PARSER;
	}

}
