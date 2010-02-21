package ee.stacc.productivity.edsl.checkers;

import org.eclipse.core.resources.IFile;

public interface IPositionDescriptor {

	IFile getFile();

	int getCharStart();

	int getCharLength();

}