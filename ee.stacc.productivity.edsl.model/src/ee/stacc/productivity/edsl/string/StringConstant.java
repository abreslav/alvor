/**
 * 
 */
package ee.stacc.productivity.edsl.string;


public class StringConstant implements IAbstractString {
	private final String constant;
	
	public StringConstant(String constant) {
		this.constant = constant;
	}

	public String toString() {
		if (constant.length() == 0) {
			return "{}";
		}
		else {
			return constant;
		}
	}
	
	public String getConstant() {
		return constant;
	}
}