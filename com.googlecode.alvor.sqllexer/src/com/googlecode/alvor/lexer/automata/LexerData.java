package com.googlecode.alvor.lexer.automata;

public class LexerData {
	public final char[] charClasses;
	public final int stateCount;
	public final int charClassCount;
	public final int[][] transitions;

	/** Attributes (state - attrs (oct)) */
	public final int[] attributes;

	/** Actions (state - action) */
	public final int[] actions;

	public final String[] keywords;
	
	/** Tokens (action - name)*/
	public final String[] tokens;
	
	public LexerData(String charClassesPacked, int stateCount, int charClassCount, int[][] transitions,
			int[] attributes, int[] actions, String[] keywords, String[] tokens) {
		this.charClasses = unpackCharClasses(charClassesPacked);
		this.stateCount = stateCount;
		this.charClassCount = charClassCount;
		this.transitions = transitions;
		this.attributes = attributes;
		this.actions = actions;
		this.keywords = keywords;
		this.tokens = tokens;
	}
	
    private static char [] unpackCharClasses(String packed) {
	    char [] map = new char[0x10000];
	    int i = 0;  /* index in packed string  */
	    int j = 0;  /* index in unpacked array */
	    int length = packed.length();
	    while (i < length) {
	      int  count = packed.charAt(i++);
	      char value = packed.charAt(i++);
	      do map[j++] = value; while (--count > 0);
	    }
	    return map;
	  }
}
