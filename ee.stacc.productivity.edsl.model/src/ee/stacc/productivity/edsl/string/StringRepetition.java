/**
 * 
 */
package ee.stacc.productivity.edsl.string;


public class StringRepetition implements IAbstractString {
	private final IAbstractString body;
	
	public StringRepetition(IAbstractString subStr) {
		this.body = subStr;
	}

	public IAbstractString getBody() {
		return body;
	}
	
	@Override
	public String toString() {
		return "(" + body + ")*";
	}

	public <R, D> R accept(IAbstractStringVisitor<? extends R,? super D> visitor, D data) {
		return visitor.visitStringRepetition(this, data);
	};
}