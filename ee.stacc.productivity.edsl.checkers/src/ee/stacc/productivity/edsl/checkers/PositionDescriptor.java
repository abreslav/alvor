package ee.stacc.productivity.edsl.checkers;

import org.eclipse.core.resources.IFile;


public class PositionDescriptor implements IPositionDescriptor {

	private final int charLength; 
	private final int charStart; 
	private final IFile file; 

	public PositionDescriptor(IFile file, int charStart, int charLength) {
		this.charLength = charLength;
		this.charStart = charStart;
		this.file = file;
	}

	@Override
	public int getCharLength() {
		return charLength;
	}

	@Override
	public int getCharStart() {
		return charStart;
	}

	@Override
	public IFile getFile() {
		return file;
	}
	
	@Override
	public String toString() {
		return file + "[" + charStart + ":" + charLength + "]";
	}

}
