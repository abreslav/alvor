package ee.stacc.productivity.edsl.testproject;

public class ForNewASE {
	void smth() {
		
		String uhuu = "uhuu";
		
		aMethod(uhuu);
		String str = "tere" + uhuu;
		
		
		
		aMethod(uhuu);
		
		//str = str + uhuu;
		/*
		str = "a";
		
		while ((uhuu = "tere").equals("tere")) {
			str = str + "x"; 
		}
		*/
		
		System.out.println(str);
	}
	
	
	void loop2() {
		String str = "a";
		String sep = "";
		while (someCond()) {
			str += "b" + sep;
		}
		str += "c";
		
		System.out.println(str);
	}
	
	String aMethod(String a) {
		return a;
	}
	
	boolean someCond() {
		return false;
	}
	
	void sb() {
		StringBuilder sb1 = new StringBuilder("");
		
		modSb(sb1);
		sb1.append("loll");
		
		System.out.println(sb1);
	}
	
	void modSb(StringBuilder sb) {
		if (someCond()) {
			sb.append("appppp");
		}
	}
}
