package ee.stacc.productivity.edsl.tracker;

import java.util.List;

public class NameUsageChoice extends NameUsage {
	private NameUsage thenUsage;
	private NameUsage elseUsage;
	
	public NameUsageChoice(NameUsage thenUsage, NameUsage elseUsage) {
		this.thenUsage = thenUsage;
		this.elseUsage = elseUsage;
	}
	
	public NameUsage getElseUsage() {
		return elseUsage;
	}
	
	public NameUsage getThenUsage() {
		return thenUsage;
	}
	

}
