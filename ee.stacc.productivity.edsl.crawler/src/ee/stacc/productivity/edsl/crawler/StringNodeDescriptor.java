package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.conntracker.ConnectionDescriptor;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;

public class StringNodeDescriptor extends NodeDescriptor implements IStringNodeDescriptor {

	private IAbstractString abstractValue;

	public StringNodeDescriptor(IPosition position,
			IAbstractString abstractValue) {
		super(position);
		if (abstractValue == null)
			throw new IllegalStateException();
		this.abstractValue = abstractValue;
	}

	public IAbstractString getAbstractValue() {
		return this.abstractValue;
	}
	
	public void setAbstractValue(IAbstractString abstractValue) {
		this.abstractValue = abstractValue;
	}
	
	@Override
	public String toString() {
		return getPosition().toString();
	}
}
