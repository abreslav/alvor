package ee.stacc.productivity.edsl.crawler;

import org.eclipse.jdt.core.dom.ASTNode;

import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.Position;

public class NodeDescriptor implements INodeDescriptor {
	private ASTNode node;
	private int lineNumber;
	private final IPosition position;
	
	public NodeDescriptor(ASTNode node, int lineNumber) {
		this.node = node;
		this.lineNumber = lineNumber;
		this.position = new Position(PositionUtil.getFileString(node), node.getStartPosition(), node.getLength());
	}

	public ASTNode getNode() {
		return node;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public IPosition getPosition() {
		return position;
	}
}
