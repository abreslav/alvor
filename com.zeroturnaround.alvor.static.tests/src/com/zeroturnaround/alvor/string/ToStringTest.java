package com.zeroturnaround.alvor.string;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ToStringTest {

	@Test
	public void test() throws Exception {
		String string = new StringSequence(
			new StringConstant("\"asdas"),
			new StringChoice(
					new StringCharacterSet("fdsa]][]"),
					new StringRepetition(new StringConstant(""))
			)
		).toString();
		assertEquals("\"\\\"asdas\" {[fdsa\\][], (\"\")+} ", string);
		
		assertEquals("\"\\\" \\n \\r\"", new StringConstant("\" \n \r").toString());
	}
}
