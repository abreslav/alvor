package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.ASTNode;

public class NameUsageLoopChoice extends NameUsage {
	private NameUsage startUsage;
	private NameUsage loopUsage;
	private ASTNode node;
	
	public NameUsageLoopChoice(ASTNode node, NameUsage startUsage, NameUsage loopUsage) {
		this.startUsage = startUsage;
		this.loopUsage = loopUsage;
		this.node = node;
	}

	public NameUsage getBaseUsage() {
		return startUsage;
	}
	
	public NameUsage getLoopUsage() {
		return loopUsage;
	}
	
	public ASTNode getNode() {
		return node;
	}
	
}
