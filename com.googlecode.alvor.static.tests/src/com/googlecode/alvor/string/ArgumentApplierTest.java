package com.googlecode.alvor.string;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringParameter;
import com.googlecode.alvor.string.StringSequence;
import com.googlecode.alvor.string.util.ArgumentApplier;

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
		
		Map<Integer, IAbstractString> args = new HashMap<Integer, IAbstractString>();
		args.put(1, new StringConstant("p0"));
		args.put(2, new StringConstant("p1"));
		args.put(3, new StringChoice(
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
				ArgumentApplier.applyArgumentsMap(abs, args).toString());
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
		
		Map<Integer, IAbstractString> args = new HashMap<Integer, IAbstractString>();
		args.put(1, new StringConstant("p0"));
		args.put(2, new StringConstant("p1"));
		
		ArgumentApplier.applyArgumentsMap(abs, args);
	}

	
	@Test
	public void testApplyZeroArguments() {
		IAbstractString abs = new StringSequence(
				new StringConstant("a"),
				new StringChoice(
						new StringConstant("x"),
						new StringConstant("y")
					),
				new StringConstant("e"));
		
		Map<Integer, IAbstractString> args = new HashMap<Integer, IAbstractString>();
		assertEquals(abs.toString(), 
				ArgumentApplier.applyArgumentsMap(abs, args).toString());
	}
}
