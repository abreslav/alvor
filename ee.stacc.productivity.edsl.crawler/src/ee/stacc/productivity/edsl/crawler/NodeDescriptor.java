package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.string.IPosition;

public class NodeDescriptor implements INodeDescriptor {
	private final IPosition position;
	
	public NodeDescriptor(IPosition position) {
		this.position = position;
	}
	
	@Override
	public IPosition getPosition() {
		return position;
	}
}
