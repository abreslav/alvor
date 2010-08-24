/**
 * 
 */
package ee.stacc.productivity.edsl.string;

/**
 * 
 * Represents repetition once or more (so it's +)
 *
 */
public class StringRepetition extends PositionedString {
	private final IAbstractString body;
	
	public StringRepetition(IAbstractString subStr) {
		this(null, subStr);
	}

	public StringRepetition(IPosition pos, IAbstractString subStr) {
		super(pos);
		this.body = subStr;
	}
	
	public IAbstractString getBody() {
		return body;
	}
	
	@Override
	public String toString() {
		return "(" + body + ")+";
	}

	public <R, D> R accept(IAbstractStringVisitor<? extends R,? super D> visitor, D data) {
		return visitor.visitStringRepetition(this, data);
	}
	
	@Override
	public boolean isEmpty() {
		return body.isEmpty();
	}
}