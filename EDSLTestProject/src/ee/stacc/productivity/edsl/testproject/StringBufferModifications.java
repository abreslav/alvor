package ee.stacc.productivity.edsl.testproject;

public class StringBufferModifications {

	public void ignoreIrrelevantStatements() {
		StringBuffer sb = new StringBuffer("a");
		sb.ensureCapacity(1000); // at the moment flags this as possible modification place
		sb.append("b");

	}
	
	void modifyStrBuf(int blaa, StringBuffer buf1, StringBuffer buf2, String bloo) {
		System.out.println("blaa");
		
		if (1 == 1) {
			buf1.append(", yo!");
		} else {
			buf1.append(", ho?");
		}
		System.out.println("bloo");
	}
	
	void testit() {
		StringBuffer sb1 = new StringBuffer("aaa");
		sb1.append("bbb");
		
		StringBuffer sb2 = new StringBuffer("111");
		
		modifyStrBuf(0, sb1, sb2, "...");
		
		sb1.append("---");
		
		sb1.append("eee");
	}
}
