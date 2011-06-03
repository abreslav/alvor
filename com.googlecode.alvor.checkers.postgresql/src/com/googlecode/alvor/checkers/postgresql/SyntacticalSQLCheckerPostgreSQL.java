package com.googlecode.alvor.checkers.postgresql;

import java.util.Collection;

import com.googlecode.alvor.checkers.CheckerException;
import com.googlecode.alvor.checkers.HotspotCheckingResult;
import com.googlecode.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.configuration.ProjectConfiguration;
import com.googlecode.alvor.lexer.automata.LexerData;
import com.googlecode.alvor.sqlparser.GLRParser;
import com.googlecode.alvor.sqlparser.GLRStack;
import com.googlecode.alvor.sqlparser.ILRParser;

public class SyntacticalSQLCheckerPostgreSQL extends SyntacticalSQLChecker {

	@Override
	protected ILRParser<GLRStack> provideParser() {
		return GLRParser.build(this.getClass().getClassLoader()
				.getResource("com/googlecode/alvor/checkers/postgresql/grammar.xml"));
	}

	@Override
	protected LexerData provideLexerData() {
		return PostgreSqlLexerData.DATA;
	}

	@Override
	public Collection<HotspotCheckingResult> checkAbstractString(
			StringHotspotDescriptor descriptor,
			ProjectConfiguration configuration) throws CheckerException {
		return super.checkAbstractString(descriptor, configuration);
	}

}
