package com.googlecode.alvor.checkers.generic;

import com.googlecode.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
import com.googlecode.alvor.lexer.automata.LexerData;
import com.googlecode.alvor.sqllexer.GenericSQLLexerData;
import com.googlecode.alvor.sqlparser.GLRStack;
import com.googlecode.alvor.sqlparser.ILRParser;
import com.googlecode.alvor.sqlparser.Parsers;

public class GenericSyntacticalSQLSyntaxChecker extends SyntacticalSQLChecker {

	@Override
	protected LexerData provideLexerData() {
		return GenericSQLLexerData.DATA;
	}

	@Override
	protected ILRParser<GLRStack> provideParser() {
		return Parsers.getGenericGLRParserForSQL();
	}
}
