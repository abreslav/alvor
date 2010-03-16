package ee.stacc.productivity.edsl.checkers;

import org.eclipse.jdt.core.dom.ASTNode;

import ee.stacc.productivity.edsl.string.IPosition;

public interface INodeDescriptor {

	ASTNode getNode();

	int getLineNumber();
	
	IPosition getPosition();

}