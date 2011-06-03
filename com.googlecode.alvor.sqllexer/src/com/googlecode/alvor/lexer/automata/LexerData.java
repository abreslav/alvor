package com.googlecode.alvor.lexer.automata;

public class LexerData {
	public final char[] CHAR_CLASSES;
	public final int STATE_COUNT;
	public final int CHAR_CLASS_COUNT;
	public final int[][] TRANSITIONS;

	/** Attributes (state - attrs (oct)) */
	public final int[] ATTRIBUTES;

	/** Actions (state - action) */
	public final int[] ACTIONS;

	public final String[] KEYWORDS;
	
	/** Tokens (action - name)*/
	public final String[] TOKENS;
	
	public LexerData(String charClassesPacked, int stateCount, int charClassCount, int[][] transitions,
			int[] attributes, int[] actions, String[] keywords, String[] tokens) {
		this.CHAR_CLASSES = unpackCharClasses(charClassesPacked);
		this.STATE_COUNT = stateCount;
		this.CHAR_CLASS_COUNT = charClassCount;
		this.TRANSITIONS = transitions;
		this.ATTRIBUTES = attributes;
		this.ACTIONS = actions;
		this.KEYWORDS = keywords;
		this.TOKENS = tokens;
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
