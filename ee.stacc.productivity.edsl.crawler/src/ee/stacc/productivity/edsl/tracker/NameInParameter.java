package ee.stacc.productivity.edsl.tracker;

public class NameInParameter extends NameUsage {
	private int index;
	
	public NameInParameter(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
