package ee.stacc.productivity.edsl.checkers;

import org.eclipse.jdt.core.dom.ASTNode;

public interface INodeDescriptor extends IPositionDescriptor {

	ASTNode getNode();

	int getLineNumber();

}