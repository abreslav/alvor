package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.string.IPosition;

public class UnsupportedNodeDescriptor extends NodeDescriptor {
	private String problemMessage;
	
	public UnsupportedNodeDescriptor(IPosition position, String problemMessage) {
		super(position);
		this.problemMessage = problemMessage;
	}

	public String getProblemMessage() {
		return problemMessage;
	}

}
