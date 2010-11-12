package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.PositionedString;

@Deprecated
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

	@Override
	public boolean containsRecursion() {
		return true;
	}
	
}
