package com.zeroturnaround.alvor.lexer.automata;

/**
 * Converts codes between alphabets
 * 
 * @author abreslav
 *
 */
public interface IAlphabetConverter {

	/**
	 * Performs identity conversion
	 */
	IAlphabetConverter ID = new IAlphabetConverter() {
		
		@Override
		public int convert(int c) {
			return c;
		}
	};
	
	/**
	 * Returns the code for the given character in the target alphabet
	 * @param c code in the source alphabet
	 * @return code in the target alphabet
	 */
	int convert(int c);
}
