/**
 * 
 */
package com.googlecode.alvor.checkers.sqlstatic;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.automata.IInputItemFactory;
import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.StringCharacterSet;
import com.googlecode.alvor.string.StringConstant;

/**
 * A Unicode character together with its {@link IPosition Position}
 * 
 * @author abreslav
 *
 */
public final class PositionedCharacter implements IAbstractInputItem {
	
	/**
	 * Creates positioned characters for given abstract strings
	 */
	public static final IInputItemFactory FACTORY = new IInputItemFactory() {
		
		@Override
		public IAbstractInputItem createInputItem(StringCharacterSet set,
				int character) {
			IPosition position = set.getPosition();
			// TODO: passing zero as indexInString is not very honest, and may cause numbers to be 
			// underlined only partially
			return new PositionedCharacter(character, position, 0, position.getLength());
		}
		
		@Override
		public IAbstractInputItem[] createInputItems(StringConstant constant) {
			IAbstractInputItem[] result = new IAbstractInputItem[constant.getConstant().length()];

			String escapedValue = constant.getEscapedValue();

			JavaStringLexer.tokenizeJavaString(escapedValue, result, constant.getPosition());
			return result;
		}

	};

	private final int code;
	private final IPosition stringPosition;
	private final int indexInString;
	private final int lengthInSource;
	
	/**
	 * @param code character code (Unicode)
	 * @param stringPosition position of the abstract string containing this character
	 * @param indexInString position of the character inside the string
	 * @param lengthInSource length of the character in the source file (might be greater 
	 * than one, consider '\n' or '\u0001')
	 */
	public PositionedCharacter(int code,
			IPosition stringPosition, int indexInString,
			int lengthInSource) {
		this.code = code;
		this.stringPosition = stringPosition;
		this.indexInString = indexInString;
		this.lengthInSource = lengthInSource;
	}

	/**
	 * Character code (Unicode)
	 */
	@Override
	public int getCode() {
		return code;
	}
	
	/**
	 * Position of this character in the innermost abstract string in which it appears 
	 * @return
	 */
	public int getIndexInString() {
		return indexInString;
	}
	
	/**
	 * Length of this character in the source file. Might be greater 
	 * than one, consider '\n' or '\u0001'.
	 */
	public int getLengthInSource() {
		return lengthInSource;
	}
	
	/**
	 * Position of the innermost abstract string containing this caharacter
	 */
	public IPosition getStringPosition() {
		return stringPosition;
	}

	@Override
	public String toString() {
		return ((char) code) + "[" + stringPosition + ";" + indexInString + ":" + lengthInSource + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		result = prime * result + indexInString;
		result = prime * result + lengthInSource;
		result = prime * result
				+ ((stringPosition == null) ? 0 : stringPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionedCharacter other = (PositionedCharacter) obj;
		if (code != other.code)
			return false;
		if (indexInString != other.indexInString)
			return false;
		if (lengthInSource != other.lengthInSource)
			return false;
		if (stringPosition == null) {
			if (other.stringPosition != null)
				return false;
		} else if (!stringPosition.equals(other.stringPosition))
			return false;
		return true;
	}
	
}
