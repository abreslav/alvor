/**
 * 
 */
package com.googlecode.alvor.string;

public class StringConstant extends PositionedString {
	private final String constant;
	private final String escaped;

	public StringConstant(String constant) {
		this(null, constant, constant);
		assert constant != null;
	}

	public StringConstant(IPosition pos, String constant, String escaped) {
		super(pos);
		this.constant = constant;
		this.escaped = escaped;
		assert constant != null;
	}
	
	public String toString() {
		if (constant.length() == 0) {
			return "\"\"";
		} else {
			return "\"" + 
				constant
					.replaceAll("\\\\", "\\\\\\\\") // \ -> \\
					.replaceAll("\\\"", "\\\\\\\"") // " -> \"
					.replaceAll("\n", "\\\\n") // NL -> \n
					.replaceAll("\r", "\\\\r") // CR -> \n
				+ "\"";
		}
	}

	public String getConstant() {
		return constant;
	}

	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		return visitor.visitStringConstant(this, data);
	}
	
	@Override
	public boolean isEmpty() {
		return constant.isEmpty();
	}
	
	public String getEscapedValue() {
		return escaped;
	}

	@Override
	public boolean containsRecursion() {
		return false;
	}
}
