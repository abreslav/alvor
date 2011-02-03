package com.zeroturnaround.alvor.lexer.alphabet;

/**
 * Represents an input item of an automaton.
 * 
 * Input items can be Unicode characters, tokens or (theoretically) even more complicated structures.
 * An item is identified by its code.
 * 
 * @author abreslav
 * 
 */
public interface IAbstractInputItem {

	/**
	 * End of file item
	 */
	IAbstractInputItem EOF = new IAbstractInputItem() {
		
		@Override
		public int getCode() {
			return -1;
		}
		
		public String toString() {
			return "EOF";
		};
	};
	
	/**
	 * @return the code representing the type of this item (e.g., Unicode location or token code)
	 */
	int getCode();
}
