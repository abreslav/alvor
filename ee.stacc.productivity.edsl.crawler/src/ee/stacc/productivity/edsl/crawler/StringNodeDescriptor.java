package ee.stacc.productivity.edsl.crawler;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.string.IAbstractString;

public class StringNodeDescriptor extends NodeDescriptor implements IStringNodeDescriptor {

	private IAbstractString abstractValue;

	public StringNodeDescriptor(ASTNode node, IFile file, int lineNumber,
			int charStart, int charLength, IAbstractString abstractValue) {
		super(node, file, lineNumber, charStart, charLength);
		this.abstractValue = abstractValue;
	}
	
	public IAbstractString getAbstractValue() {
		return this.abstractValue;
	}
	
	public void setAbstractValue(IAbstractString abstractValue) {
		this.abstractValue = abstractValue;
	}
	
}
