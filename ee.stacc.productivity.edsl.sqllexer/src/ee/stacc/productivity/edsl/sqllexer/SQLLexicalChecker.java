package ee.stacc.productivity.edsl.sqllexer;

import java.io.IOException;
import java.io.Reader;

public class SQLLexicalChecker {

	public static String check(Reader sql) {
		SQLLexer lexer = new SQLLexer(sql);
		do {
			Yytoken token;
			try {
				token = lexer.yylex();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			
			switch (token.m_index) {
				case -1: // EOF
					return "";
				case -2: // Error
					return token.m_text;
			}
		} while (true);
	}
}
