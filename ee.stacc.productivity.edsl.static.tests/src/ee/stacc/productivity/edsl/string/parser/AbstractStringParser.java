package ee.stacc.productivity.edsl.string.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;

public class AbstractStringParser {

	private final AbstractStringLexer lexer;

	public AbstractStringParser(AbstractStringLexer lexer) {
		this.lexer = lexer;
	}
	
	/*
	 * strings ::= seqeunce*
	 * sequence ::= item+ (EOF | NEWLINE)
	 * item ::= const | charSet | choice | repetition 
	 * const ::= CONSTANT
	 * charSet ::= CHAR_SET
	 * choice ::= OPEN_CURLY seqeunce (, sequence)* CLOSE_CURLY
	 * repetition ::= OPEN_REP sequence CLOSE_REP
	 */
	
	public List<IAbstractString> strings() {
		List<IAbstractString> result = new ArrayList<IAbstractString>();
		while(true) {
			Token token = next();
			while (token == Token.NEWLINE) {
				token = next();
			}
			if (token == Token.EOF) {
				break;
			}
			back();
			IAbstractString abstractString = abstractString();
			result.add(abstractString);
		}
		return result;
	}
	
	public IAbstractString abstractString() {
		List<IAbstractString> result = new ArrayList<IAbstractString>();
		do {
			Token token = next();
			if (token == Token.EOF 
					|| token == Token.NEWLINE 
					|| token == Token.COMMA 
					|| token == Token.CLOSE_ITER 
					|| token == Token.CLOSE_CURLY) {
				back();
				break;
			}
			back();
			result.add(item());
		} while (true);
		return new StringSequence(result);
	}

	private void back() {
		lexer.yypushback(lexer.yytext().length());
	}

	private IAbstractString item() {
		Token token = next();
		switch (token.getType()) {
		case CONSTANT:
			return new StringConstant(token.getText());
		case CHAR_SET:
			return new StringCharacterSet(token.getText());
		case OPEN_CURLY:
			back();
			return choice();
		case OPEN_ITER:
			back();
			return iteration();
		}
		throw new ParseException("Unexpected token: " + token);
	}

	private IAbstractString iteration() {
		match(Token.OPEN_ITER);
		IAbstractString result = abstractString();
		match(Token.CLOSE_ITER);
		return new StringRepetition(result);
	}
	
	private IAbstractString choice() {
		match(Token.OPEN_CURLY);
		List<IAbstractString> result = new ArrayList<IAbstractString>();
		while (true) {
			IAbstractString sequence = abstractString();
			result.add(sequence);
			Token next = next();
			if (next == Token.CLOSE_CURLY) {
				break;
			}
			back();
			match(Token.COMMA);
		}
		return new StringChoice(result);
	}

	private Token next() {
		try {
			Token yylex = lexer.yylex();
			return yylex;
		} catch (IOException e) {
			throw new ParseException(e);
		}
	}

	private void match(Token expected) {
		if (next() != expected) {
			throw new ParseException("Missing token: " + expected);
		}
	}

	public static List<IAbstractString> parseFile(String fileName) throws FileNotFoundException {
		AbstractStringLexer lexer = new AbstractStringLexer(new FileReader(fileName));
		AbstractStringParser parser = new AbstractStringParser(lexer);
		return parser.strings();
	}
}
