package ee.stacc.productivity.edsl.checkers;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

public interface INodeDescriptor {

	ASTNode getNode();

	IFile getFile();

	int getLineNumber();

	int getCharStart();

	int getCharLength();

}