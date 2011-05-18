package com.googlecode.alvor.sqlparser;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Describes a lexical analyzer specification
 * 
 * @author abreslav
 */
public final class LexerSpec {
	private static Comparator<String> LENGTH_DECREASING_ORDER = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o2.length() - o1.length();
		}
	};

	private final List<String> helpers = new ArrayList<String>();
	private final List<String> literals = new ArrayList<String>();
	private final List<String> keywords = new ArrayList<String>();
	private final List<String> tokens = new ArrayList<String>();

	public void printTokens(PrintWriter out) {
		Collections.sort(literals, LENGTH_DECREASING_ORDER);
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
