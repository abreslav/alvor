package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.PositionedString;

public class NamedString extends PositionedString {

	private Object key;
	private IAbstractString body;
	
	
	public NamedString(IPosition pos, Object key, IAbstractString body) {
		super(pos);
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
		if (visitor instanceof IAbstractStringVisitorExtended<?, ?>) {
			@SuppressWarnings("unchecked")
			IAbstractStringVisitorExtended<R, D> visitorEx = (IAbstractStringVisitorExtended<R, D>) visitor;
			return visitorEx.visitNamedString(this, data);			
		}
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
