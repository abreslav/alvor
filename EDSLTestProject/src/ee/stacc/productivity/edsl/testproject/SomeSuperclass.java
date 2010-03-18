package ee.stacc.productivity.edsl.testproject;

public class SomeSuperclass {
	protected static final String STR1 = "hohoho";
	
	String getSomeStr() {
		return "SomeSuperclass.getSomeStr()";
	}
	
	static String getSomeStr(int i) {
		return "SomeSuperclass.getSomeStr(int i)";
	}
}
