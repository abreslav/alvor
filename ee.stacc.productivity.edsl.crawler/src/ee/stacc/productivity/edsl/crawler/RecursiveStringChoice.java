package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.IPosition;

public class RecursiveStringChoice implements IAbstractString {
	private IAbstractString base;
	private IAbstractString rec; // pointer to a parent node of this node
	
	public RecursiveStringChoice(IAbstractString base, IAbstractString rec) {
		this.base = base;
		this.rec = rec;
	}

	public IAbstractString getBase() {
		return base;
	}
	
	public IAbstractString getRec() {
		return rec;
	}
	
	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPosition getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return "<<<" + base.toString() + ">>>";
	}

}
