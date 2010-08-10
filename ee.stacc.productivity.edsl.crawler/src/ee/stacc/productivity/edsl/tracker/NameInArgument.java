package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class NameInArgument extends NameUsage {
	MethodInvocation inv;
	int index;
	
	public NameInArgument(MethodInvocation inv, int index) {
		this.inv = inv;
		this.index = index;
	}

	public MethodInvocation getInv() {
		return inv;
	}
	
	public int getIndex() {
		return index;
	}
	
	public ASTNode getNode() {
		return inv;
	}
	
}
