package ee.stacc.productivity.edsl.tracker;

public class NameUsageLoopChoice extends NameUsage {
	private NameUsage startUsage;
	private NameUsage loopUsage;
	
	public NameUsageLoopChoice(NameUsage startUsage, NameUsage loopUsage) {
		this.startUsage = startUsage;
		this.loopUsage = loopUsage;
	}

	public NameUsage getBaseUsage() {
		return startUsage;
	}
	
	public NameUsage getLoopUsage() {
		return loopUsage;
	}
	
}
