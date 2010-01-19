import java.io.StringReader;

import ee.stacc.productivity.edsl.sqllexer.SQLLexicalChecker;


public class Main {

	public static void main(String[] args) {
		SQLLexicalChecker.check(new StringReader("SELECT a 1FROM b \"'"));
	}
}
