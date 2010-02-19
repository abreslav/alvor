package ee.stacc.productivity.edsl.crawler;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

public class UnsupportedNodeDescriptor extends NodeDescriptor {
	private String problemMessage;
	public UnsupportedNodeDescriptor(ASTNode node, IFile file, int lineNumber,
			int charStart, int charLength, String problemMessage) {
		super(node, file, lineNumber, charStart, charLength);
		this.problemMessage = problemMessage;
	}
	
	public String getProblemMessage() {
		return problemMessage;
	}

}
