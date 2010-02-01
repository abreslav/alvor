package ee.stacc.productivity.edsl.crawler;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

public class NodeDescriptor {
	private ASTNode node;
	private IFile file;
	private int lineNumber;
	private int charStart;
	private int charLength;
	
	public NodeDescriptor(ASTNode node, IFile file, int lineNumber,
			int charStart, int charLength) {
		this.node = node;
		this.file = file;
		this.lineNumber = lineNumber;
		this.charStart = charStart;
		this.charLength = charLength;
	}

	public ASTNode getNode() {
		return node;
	}
	
	public IFile getFile() {
		return file;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public int getCharStart() {
		return charStart;
	}
	
	public int getCharLength() {
		return charLength;
	}
}
