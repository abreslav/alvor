package ee.stacc.productivity.edsl.crawler;

import org.eclipse.jdt.core.dom.ASTNode;

public class UnsupportedNodeDescriptor extends NodeDescriptor {
	private String problemMessage;
	public UnsupportedNodeDescriptor(ASTNode node, int lineNumber, String problemMessage) {
		super(node, lineNumber);
		this.problemMessage = problemMessage;
	}
	
	public String getProblemMessage() {
		return problemMessage;
	}

}
