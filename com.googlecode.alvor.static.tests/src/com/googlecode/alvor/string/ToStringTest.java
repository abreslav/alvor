package com.googlecode.alvor.string;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.googlecode.alvor.string.StringCharacterSet;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringRepetition;
import com.googlecode.alvor.string.StringSequence;


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
