package ee.stacc.productivity.edsl.crawler;

import org.eclipse.jdt.core.dom.ASTNode;

import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.string.IAbstractString;

public class StringNodeDescriptor extends NodeDescriptor implements IStringNodeDescriptor {

	private IAbstractString abstractValue;

	public StringNodeDescriptor(ASTNode node, int lineNumber, IAbstractString abstractValue) {
		super(node, lineNumber);
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
		return getPosition().getPath() + ":" + getLineNumber();
	}
}
