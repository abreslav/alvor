package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

public class NameUsage {
	private ASTNode node;
	private Statement mainStatement;
	
	public ASTNode getNode() {
		return node;
	}
	
	public Statement getMainStatement() {
		return mainStatement;
	}
}

