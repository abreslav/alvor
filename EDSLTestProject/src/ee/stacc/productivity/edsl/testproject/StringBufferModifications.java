package ee.stacc.productivity.edsl.testproject;

public class StringBufferModifications {

	public void ignoreIrrelevantStatements() {
		StringBuffer sb = new StringBuffer("a");
		sb.ensureCapacity(1000); // at the moment flags this as possible modification place
		sb.append("b");

	}
}
