/**
 * 
 */
package ee.stacc.productivity.edsl.string;

import java.util.Iterator;
import java.util.List;

public class StringChoice extends AbstractStringCollection implements IAbstractString {

	public StringChoice(IAbstractString... options) {
		super(options);
	}

	public StringChoice(List<? extends IAbstractString> options) {
		super(options);
	}

	public StringChoice(IPosition pos, IAbstractString... options) {
		super(pos, options);
	}
	
	public StringChoice(IPosition pos, List<? extends IAbstractString> options) {
		super(pos, options);
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("{");
		for (Iterator<IAbstractString> i = getItems().iterator(); i.hasNext();) {
			IAbstractString option = i.next();
			stringBuilder.append(option);
			if (i.hasNext()) {
				stringBuilder.append(", ");
			}
		}
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

	public <R, D> R accept(IAbstractStringVisitor<? extends R,? super D> visitor, D data) {
		return visitor.visitStringChoice(this, data);
	};
}