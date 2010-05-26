package ee.stacc.productivity.edsl.common.logging;

public class Measurements {
	static public SumTimer methodDeclSearchTimer = new SumTimer("Search method decl");
	static public SumTimer argumentSearchTimer = new SumTimer("Search arguments");
	static public SumTimer parseTimer = new SumTimer("Parse");
	
	static public void resetAll() {
		methodDeclSearchTimer.reset();
		argumentSearchTimer.reset();
		parseTimer.reset();
	}
}
