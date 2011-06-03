package com.googlecode.alvor.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.googlecode.alvor.sqlparser.ParserSimulator;
import com.googlecode.alvor.string.IAbstractString;


public class ParserPerformanceTest {

	@Test
	public void test() throws Exception {
		
		Set<String> strings = new HashSet<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader("data/earved_all.txt"));
		do {
			String readLine = reader.readLine();
			if (readLine == null) {
				 break;
			}
			if (!strings.add(readLine)) {
				System.out.println(readLine);
			}
		} while (true);
		reader.close();
		
//		System.out.println("Lines: " + count);
//		System.out.println("Unique: " + strings.size());
		
//		List<IAbstractString> all = AbstractStringParser.parseFile("data/earved_all.txt");
//		System.out.println("Strings: " + all.size());

//		doTest(all);
//		
//		doTest(all);
//		doTest(all);
//		doTest(all);
		
	}

	public void doTest(List<IAbstractString> all) {
		ParserSimulator.getGenericSqlLALRInstance().allTime = 0;
		long time = System.nanoTime();
		for (IAbstractString as : all) {
			ParserSimulator.getGenericSqlLALRInstance().check(as);
		}
		printNano("All checks: ", (System.nanoTime() - time));
		printNano("Syntax: ", ParserSimulator.getGenericSqlLALRInstance().allTime);
	}

	private void printNano(String string, long x) {
		System.out.println(string + 
				(x / 1000000000.0)
		);
	}
}
