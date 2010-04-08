package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.IPosition;

public class NamedString implements IAbstractString {

	private Object key;
	private IAbstractString body;
	
	
	public NamedString(Object key, IAbstractString body) {
		this.key = key;
		this.body = body;
	}

	public IAbstractString getBody() {
		return body;
	}
	
	public Object getKey() {
		return key;
	}
	
	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPosition getPosition() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "Named(" + key.hashCode() + ", " + body.toString() +  ")";
	}
	
}
