package com.googlecode.alvor.sqlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates specifications of lexer and parser
 * 
 * @author abreslav
 *
 */
public class SpecGenerator {

	public static final SpecGenerator INSTANCE = new SpecGenerator();
	
/*
%lex-keyword ABBBC -> %token ABBC + entry in .keywords file
%lex-helper ABC = ... -> + ABC = ...
%lex-token NAME = ... -> %token NAME + ...{/*NMAE* /}
%lex-token "lit" -> + "lit"{/*lit* /}
%lex-whitespace ... -> + ..."{/** /}
 */
	private static final String LEX_KEYWORD = "%lex-keyword";
	private static final String LEX_HELPER = "%lex-helper";
	private static final String LEX_TOKEN = "%lex-token";
	private static final String LEX_LITERAL = "%lex-literal";
	private static final String LEX_WHITESPACE = "%lex-whitespace";
	private static final Pattern PATTERN_TOKEN_NAME = Pattern.compile(LEX_TOKEN + "\\s*([a-zA-Z_]+)");
	private static final Pattern PATTERN_TOKEN_VALUE = Pattern.compile(LEX_TOKEN + "\\s*[a-zA-Z_]+\\s*=\\s*(.*?)$");
	private static final Pattern PATTERN_HELPER_NAME = Pattern.compile(LEX_HELPER + "\\s*([a-zA-Z_]+)");
	private static final Pattern PATTERN_HELPER_VALUE = Pattern.compile(LEX_HELPER + "\\s*[a-zA-Z_]+\\s*=\\s*(.*?)$");
	private static final Pattern PATTERN_WHITESPACE_VALUE = Pattern.compile(LEX_WHITESPACE + "\\s*(.*?)$");
	private static final Pattern PATTERN_LITERAL = Pattern.compile(LEX_LITERAL + "\\s*\"([^\"]*)\"");
	private static final Pattern PATTERN_KEYWORD = Pattern.compile(LEX_KEYWORD + "\\s*([a-zA-Z_]+)");

	// Singleton
	private SpecGenerator() {}
	
	/**
	 * Reads a syndicated specification of lexer and parser, writes Bison grammar to a file and creates 
	 * a {@link LexerSpec} object for lexer specification  
	 * @param in syndicated grammar specification
	 * @param out Bison grammar
	 * @return an object describing a corresponding lexical analyzer
	 * @throws IOException
	 */
	public LexerSpec processParserSpec(BufferedReader in, PrintWriter out) throws IOException {
		LexerSpec lexerSpec = new LexerSpec();
		String line;
		while ((line = in.readLine()) != null) {
			if (line.startsWith(LEX_KEYWORD)) {
				String keyword = getKeyword(line);
				out.println("%token " + keyword);
				lexerSpec.addKeyword(keyword);
			} else if (line.startsWith(LEX_HELPER)) {
				lexerSpec.addHelper(getHelperName(line), getHelperValue(line));
			} else if (line.startsWith(LEX_LITERAL)) {
				lexerSpec.addLiteral(getLiteral(line));
			} else if (line.startsWith(LEX_TOKEN)) {
				String name = getTokenName(line);
				out.println("%token " + name);
				lexerSpec.addToken(name, getTokenValue(line));
			} else if (line.startsWith(LEX_WHITESPACE)) {
				lexerSpec.addToken("", getWhitespace(line));
			} else {
				out.println(line);
			}
		}
		return lexerSpec;
	}

	/**
	 * Generates a JFlex lexical analyzer specification
	 * @param lexerSpec an object describing a lexical analyzer
	 * @param template a template file for lexical analyzers, must contain a line starting with 
	 * "%%%helpers%%" to indicate where to write helper definitions, and a line starting with 
	 * "%%%tokens%%" to indicate where to put token definitions. Both special lines will be removed.  
	 * @param out resulting lexer specification
	 * @throws IOException
	 */
	public void generateLexerSpec(LexerSpec lexerSpec, BufferedReader template, PrintWriter out) throws IOException {
		String line;
		while ((line = template.readLine()) != null) {
			String outLine = line;
			if (line.startsWith("%%%helpers%%%")) {
				lexerSpec.printHelpers(out);
			} else if (line.startsWith("%%%tokens%%%")) {
				lexerSpec.printTokens(out);
			} else {
				out.println(outLine);
			}
		}
	}
	
	private String getTokenValue(String line) {
		return getFirstGroupMatch(line, PATTERN_TOKEN_VALUE);
	}

	private String getTokenName(String line) {
		return getFirstGroupMatch(line, PATTERN_TOKEN_NAME);
	}

	private String getHelperValue(String line) {
		return getFirstGroupMatch(line, PATTERN_HELPER_VALUE);
	}
	
	private String getHelperName(String line) {
		return getFirstGroupMatch(line, PATTERN_HELPER_NAME);
	}
	
	private String getLiteral(String line) {
		return getFirstGroupMatch(line, PATTERN_LITERAL);
	}

	private String getKeyword(String line) {
		return getFirstGroupMatch(line, PATTERN_KEYWORD);
	}

	private String getWhitespace(String line) {
		return getFirstGroupMatch(line, PATTERN_WHITESPACE_VALUE);
	}
	
	private String getFirstGroupMatch(String line, Pattern pattern) {
		Matcher matcher = pattern.matcher(line);
		if (!matcher.find()) {
			throw new FormatException();
		}
		return matcher.group(1);
	}
	
	@SuppressWarnings("serial")
	private static final class FormatException extends RuntimeException {

		public FormatException() {
			super();
		}

		@SuppressWarnings("unused")
		public FormatException(String message) {
			super(message);
		}
		
	}
}
