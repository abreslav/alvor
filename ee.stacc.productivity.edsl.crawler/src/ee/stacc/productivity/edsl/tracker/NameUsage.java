package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.ASTNode;

abstract public class NameUsage {
	private ASTNode node;
	
	abstract public ASTNode getNode();
	
}

