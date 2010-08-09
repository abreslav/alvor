package ee.stacc.productivity.edsl.common.logging;

public class Measurements {
	static public SumTimer methodDeclSearchTimer = new SumTimer("Search method decl");
	static public SumTimer argumentSearchTimer = new SumTimer("Search arguments");
	static public SumTimer parseTimer = new SumTimer("Parse");
	static public SumTimer uiTimer = new SumTimer("UI");
	static public SumTimer stringCollectionTimer = new SumTimer("String collection");
	
	static public void resetAll() {
		methodDeclSearchTimer.reset();
		argumentSearchTimer.reset();
		parseTimer.reset();
		uiTimer.reset();
		stringCollectionTimer.reset();
	}
}
