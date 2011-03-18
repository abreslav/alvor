package com.zeroturnaround.alvor.common;

import org.junit.Test;

import com.zeroturnaround.alvor.common.EmptyStringConstant;
import com.zeroturnaround.alvor.common.RecursionConverter;
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
				new StringRecursion(pos)
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
					new StringRecursion(pos)
				)
			)
				
		);
		
		System.out.println("BEFORE: " + str);
		System.out.println("AFTER: " + RecursionConverter.recursionToRepetition(str));
	}
	
	@Test
	public void test3() {
		System.out.println("test3");
		IPosition pos = new Position("p1", 0, 0);
		
		IAbstractString str = new StringSequence (
			pos,
			new StringRecursion(pos),
			new StringChoice(
					new StringConstant("c"),
					new EmptyStringConstant()
			),
			new StringConstant("?")
		);
		
		System.out.println("BEFORE: " + str);
		System.out.println("AFTER: " + RecursionConverter.recursionToRepetition(str));
	}
}
