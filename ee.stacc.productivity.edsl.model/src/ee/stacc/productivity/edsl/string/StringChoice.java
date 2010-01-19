/**
 * 
 */
package ee.stacc.productivity.edsl.string;

import java.util.List;

public class StringChoice extends AbstractStringCollection implements IAbstractString {

	public StringChoice(IAbstractString... options) {
		super(options);
	}

	public StringChoice(List<IAbstractString> options) {
		super(options);
	}

	public String toString() {
		return getItems().toString().replace('[', '{').replace(']', '}');
	}
}