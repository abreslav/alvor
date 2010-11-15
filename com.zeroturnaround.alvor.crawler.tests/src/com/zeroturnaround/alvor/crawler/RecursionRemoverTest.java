package com.zeroturnaround.alvor.crawler;

import org.junit.Test;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.Position;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringSequence;

public class RecursionRemoverTest {
	@Test
	public void test1() {
		System.out.println("test1");
		IPosition pos = new Position("p1", 0, 0);
		
		IAbstractString str = new StringChoice (
			pos,
			new StringConstant("b"),
			new StringSequence(
				new StringConstant("a"),
				new StringRecursion(null, pos)
			)
				
		);
		
		System.out.println("BEFORE: " + str);
		System.out.println("AFTER: " + RecursionConverter.recursionToRepetition(str));
	}
	
	@Test
	public void test2() {
		System.out.println("test2");
		IPosition pos = new Position("p1", 0, 0);
		
		IAbstractString str = new StringChoice (
			pos,
			new StringConstant("b"),
			new StringSequence(
				new StringConstant("a"),
				new StringChoice(
					new StringConstant("c"),
					new StringRecursion(null, pos)
				)
			)
				
		);
		
		System.out.println("BEFORE: " + str);
		System.out.println("AFTER: " + RecursionConverter.recursionToRepetition(str));
	}
}
