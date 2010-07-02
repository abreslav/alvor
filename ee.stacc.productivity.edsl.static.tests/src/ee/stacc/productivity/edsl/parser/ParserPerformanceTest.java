package ee.stacc.productivity.edsl.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.ParserSimulator;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;


public class ParserPerformanceTest {

	@Test
	public void test() throws Exception {
		
		Set<String> strings = new HashSet<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader("data/earved_all.txt"));
		int count = 0;
		do {
			String readLine = reader.readLine();
			if (readLine == null) {
				 break;
			}
			count++;
			if (!strings.add(readLine)) {
				System.out.println(readLine);
			}
		} while (true);
		reader.close();
		
//		System.out.println("Lines: " + count);
//		System.out.println("Unique: " + strings.size());
		
		List<IAbstractString> all = AbstractStringParser.parseFile("data/earved_all.txt");
//		System.out.println("Strings: " + all.size());

//		doTest(all);
//		
//		doTest(all);
//		doTest(all);
//		doTest(all);
		
	}

	private void doTest(List<IAbstractString> all) {
		ParserSimulator.LALR_INSTANCE.allTime = 0;
		long time = System.nanoTime();
		for (IAbstractString as : all) {
			ParserSimulator.LALR_INSTANCE.check(as);
		}
		printNano("All checks: ", (System.nanoTime() - time));
		printNano("Syntax: ", ParserSimulator.LALR_INSTANCE.allTime);
	}

	private void printNano(String string, long x) {
		System.out.println(string + 
				(x / 1000000000.0)
		);
	}
}
