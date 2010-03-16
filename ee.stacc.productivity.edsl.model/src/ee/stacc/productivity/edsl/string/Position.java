package ee.stacc.productivity.edsl.string;

public class Position implements IPosition {

	private final String path;
	private final int start;
	private final int length;
	
	public Position(String path, int start, int length) {
		this.path = path;
		this.start = start;
		this.length = length;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public int getStart() {
		return start;
	}

}
