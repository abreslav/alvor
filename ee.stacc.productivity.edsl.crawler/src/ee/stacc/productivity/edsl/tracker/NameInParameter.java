package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.ASTNode;

public class NameInParameter extends NameUsage {
	private int index;
	
	public NameInParameter(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
	
	public ASTNode getASTNode() {
		throw new UnsupportedOperationException();
	}
	
}
