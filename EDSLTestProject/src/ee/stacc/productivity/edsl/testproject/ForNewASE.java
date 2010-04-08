package ee.stacc.productivity.edsl.testproject;

public class ForNewASE {
	void smth() {
		
		String uhuu = "uhuu";
		
		String str = "tere" + uhuu;
		
		
		
		//aMethod(uhuu);
		
		str = "a";
		
		while ((uhuu = "tere").equals("tere")) {
			str = str + "x"; 
		}
		
		System.out.println(str);
	}
	
	String aMethod(String a) {
		return a;
	}
}
