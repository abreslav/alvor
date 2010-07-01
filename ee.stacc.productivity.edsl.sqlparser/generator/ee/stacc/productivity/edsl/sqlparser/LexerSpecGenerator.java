package ee.stacc.productivity.edsl.sqlparser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexerSpecGenerator {

	public static final LexerSpecGenerator INSTANCE = new LexerSpecGenerator();
	
/*
%lex-keyword ABBBC -> %token ABBC + "UPDATE" {/*UPDATE* /}
%lex-helper ABC = ... -> + ABC = ...
%lex-token NAME = ... -> %token NAME + ...{/*NMAE* /}
%lex-token "lit" -> + "lit"{/*lit* /}
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

	public static void main(String[] args) throws FileNotFoundException, IOException {
//		System.out.println(INSTANCE.getTokenName("%lex-token  NAME = adsfas"));
//		System.out.println(INSTANCE.getTokenValue("%lex-token  NAME = adsfas"));
//		System.out.println(INSTANCE.getHelperValue("%lex-helper  NAME = adsfas"));
//		System.out.println(INSTANCE.getHelperName("%lex-helper  NAME = adsfas"));
//		System.out.println(INSTANCE.getLiteral("%lex-literal \"sdf\""));
//		System.out.println(INSTANCE.getKeyword("%lex-keyword ASDCsda"));
//		System.out.println(INSTANCE.getWhitespace("%lex-whitespace ASDCsda"));
		
		String parserTempName = "grammar/sql.bgtemplate";
		String parserOutName = "grammar/sql.bg";
		String lexerTempName = "grammar/sql.flextemplate";
		String lexerOutName = "grammar/sql.flex";
		String keywordsName = "grammar/sql.keywords";
		
		if (args.length >= 5) {
			parserTempName = args[0];
			parserOutName = args[1];
			lexerTempName = args[2];
			lexerOutName = args[3];
			keywordsName = args[4];
		}
		
		BufferedReader in = new BufferedReader(new FileReader(parserTempName));
		PrintWriter out = new PrintWriter(parserOutName);
		LexerSpec lexerSpec = INSTANCE.processParserSpec(in, out);
		in.close();
		out.close();
		
		in = new BufferedReader(new FileReader(lexerTempName));
		out = new PrintWriter(lexerOutName);
		INSTANCE.generateLexerSpec(lexerSpec, in, out);
		out.close();
		in.close();

		out = new PrintWriter(keywordsName);
		lexerSpec.printKeywords(out);
		out.close();
	}

	private LexerSpecGenerator() {}
	
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
	
	private static final class LexerSpec {
		private final List<String> helpers = new ArrayList<String>();
		private final List<String> literals = new ArrayList<String>();
		private final List<String> keywords = new ArrayList<String>();
		private final List<String> tokens = new ArrayList<String>();

		public void printTokens(PrintWriter out) {
			Collections.sort(literals, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o2.length() - o1.length();
				}
			});
			printList(out, literals);
			printList(out, tokens);
		}

		public void printHelpers(PrintWriter out) {
			List<String> list = helpers;
			printList(out, list);
		}

		private void printList(PrintWriter out, List<String> list) {
			for (String helper : list) {
				out.println(helper);
			}
		}
		
		public void printKeywords(PrintWriter out) {
//			Collections.sort(keywords, Collections.reverseOrder());
			printList(out, keywords);
		}

		public void addKeyword(String keyword) {
			keywords.add(keyword);
		}
		
		public void addHelper(String name, String value) {
			helpers.add(name + " = " + value);
		}

		public void addLiteral(String literal) {
			literals.add("\"" + literal + "\" {/*" + literal + "*/}");
		}

		public void addToken(String name, String value) {
			tokens.add(value + " {/*" + name + "*/}");
		}

		
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
