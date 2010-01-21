/**
 * 
 */
package ee.stacc.productivity.edsl.string;

import java.util.List;


public class StringSequence extends AbstractStringCollection implements IAbstractString {

	public StringSequence(IAbstractString... options) {
		super(options);
	}

	public StringSequence(List<IAbstractString> options) {
		super(options);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (IAbstractString item : getItems()) {
			stringBuilder.append(item).append(" ");
		}
		return stringBuilder.toString();
	}

	public <R, D> R accept(IAbstractStringVisitor<? extends R,? super D> visitor, D data) {
		return visitor.visitStringSequence(this, data);
	};
}