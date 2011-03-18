package com.zeroturnaround.alvor.string;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.zeroturnaround.alvor.string.util.ArgumentApplier;

public class ArgumentApplierTest {

	@Test
	public void testApplyArgumentsNormal() {
		IAbstractString abs = new StringSequence(
			new StringParameter(0),
			new StringConstant("a"),
			new StringChoice(
				new StringConstant("x"),
				new StringParameter(0),
				new StringParameter(1)
				),
			new StringParameter(2),
			new StringConstant("e"));
		
		List<IAbstractString> args = new ArrayList<IAbstractString>();
		args.add(new StringConstant("p0"));
		args.add(new StringConstant("p1"));
		args.add(new StringChoice(
				new StringConstant("p2a"),
				new StringConstant("p2b")));
		
		IAbstractString result = new StringSequence(
			new StringConstant("p0"),
			new StringConstant("a"),
			new StringChoice(
				new StringConstant("x"),
				new StringConstant("p0"),
				new StringConstant("p1")
				),
			new StringChoice(
					new StringConstant("p2a"),
					new StringConstant("p2b")
				),
			new StringConstant("e"));
		
		//System.out.println("Expect: " + result.toString());
		//System.out.println("Actual: " + ArgumentApplier.applyArguments(abs, args).toString());
		
		assertEquals(result.toString(), 
				ArgumentApplier.applyArgumentsList(abs, args).toString());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void testMissingArguments() {
		IAbstractString abs = new StringSequence(
				new StringParameter(0),
				new StringConstant("a"),
				new StringChoice(
					new StringConstant("x"),
					new StringParameter(0),
					new StringParameter(2)
					),
				new StringParameter(2),
				new StringConstant("e"));
		
		List<IAbstractString> args = new ArrayList<IAbstractString>();
		args.add(new StringConstant("p0"));
		args.add(new StringConstant("p1"));
		
		ArgumentApplier.applyArgumentsList(abs, args);
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testApplyZeroArguments() {
		IAbstractString abs = new StringSequence(
				new StringConstant("a"),
				new StringChoice(
						new StringConstant("x"),
						new StringConstant("y")
					),
				new StringConstant("e"));
		
		assertEquals(abs.toString(), 
				ArgumentApplier.applyArgumentsList(abs, Collections.EMPTY_LIST).toString());
	}
}
