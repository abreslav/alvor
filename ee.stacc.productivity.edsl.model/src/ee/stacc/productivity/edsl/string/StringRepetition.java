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
}