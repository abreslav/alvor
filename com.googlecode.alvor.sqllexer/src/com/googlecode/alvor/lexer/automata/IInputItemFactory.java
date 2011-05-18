package com.googlecode.alvor.lexer.automata;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.string.StringCharacterSet;
import com.googlecode.alvor.string.StringConstant;

/**
 * Convert terminal abstract strings to {@link IAbstractInputItem}.
 * Used by {@link StringToAutomatonConverter}
 * 
 * @author abreslav
 *
 */
public interface IInputItemFactory {

	/**
	 * Create input items corresponding to the given string 
	 * @param constant terminal string
	 * @return array of resulting items
	 */
	IAbstractInputItem[] createInputItems(StringConstant constant);
	
	/**
	 * Create an input item for the given character in the given character set
	 * @param set a set the character is taken from
	 * @param character the code (Unicode) of the character or -1 for EOF
	 * @return resulting item
	 */
	IAbstractInputItem createInputItem(StringCharacterSet set, int character);
}
