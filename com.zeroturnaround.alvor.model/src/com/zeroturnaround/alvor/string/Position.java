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

	@Override
	public String toString() {
		return path + ":" + start + "(" + length + ")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + start;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (length != other.length)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (start != other.start)
			return false;
		return true;
	}
	
	

}
