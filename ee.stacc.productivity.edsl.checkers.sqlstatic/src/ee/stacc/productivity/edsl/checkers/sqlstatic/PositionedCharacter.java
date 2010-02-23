/**
 * 
 */
package ee.stacc.productivity.edsl.checkers.sqlstatic;

import ee.stacc.productivity.edsl.checkers.IPositionDescriptor;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public final class PositionedCharacter implements IAbstractInputItem {
	
	private final int code;
	private final IPositionDescriptor stringPosition;
	private final int indexInString;
	private final int lengthInSource;
	
	public PositionedCharacter(int code,
			IPositionDescriptor stringPosition, int indexInString,
			int lengthInSource) {
		this.code = code;
		this.stringPosition = stringPosition;
		this.indexInString = indexInString;
		this.lengthInSource = lengthInSource;
	}

	@Override
	public int getCode() {
		return code;
	}
	
	public int getIndexInString() {
		return indexInString;
	}
	
	public int getLengthInSource() {
		return lengthInSource;
	}
	
	public IPositionDescriptor getStringPosition() {
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