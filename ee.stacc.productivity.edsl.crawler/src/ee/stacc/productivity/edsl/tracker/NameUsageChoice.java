package ee.stacc.productivity.edsl.tracker;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class NameUsageChoice extends NameUsage {
	private NameUsage thenUsage;
	private NameUsage elseUsage;
	private ASTNode node;
	
	public NameUsageChoice(ASTNode node, NameUsage thenUsage, NameUsage elseUsage) {
		this.thenUsage = thenUsage;
		this.elseUsage = elseUsage;
		this.node = node;
	}
	
	public NameUsage getElseUsage() {
		return elseUsage;
	}
	
	public NameUsage getThenUsage() {
		return thenUsage;
	}
	
	public ASTNode getNode() {
		return node;
	}
	

}
