package com.googlecode.alvor.checkers.generic;

import com.googlecode.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
import com.googlecode.alvor.sqllexer.GenericSQLLexerData;
import com.googlecode.alvor.sqlparser.GLRStack;
import com.googlecode.alvor.sqlparser.IParserStackLike;
import com.googlecode.alvor.sqlparser.ParserSimulator;
import com.googlecode.alvor.sqlparser.Parsers;

public class GenericSyntacticalSQLSyntaxChecker extends SyntacticalSQLChecker {

	@Override
	public ParserSimulator<? extends IParserStackLike> createParserSimulator() {
		return new ParserSimulator<GLRStack>(Parsers.getGLRParserForSQL(),
				 GLRStack.FACTORY, GenericSQLLexerData.DATA);
	}
}
