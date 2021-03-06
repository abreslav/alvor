package com.googlecode.alvor.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.googlecode.alvor.common.StringConverter;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringSequence;


public class StringConverterTest {
	
	@Test
	public void testOptimizeChoice() {
		StringChoice startingVersion = new StringChoice(
				new StringConstant("a"),
				new StringSequence(
						new StringConstant("a"),
						new StringConstant("b")
				)
		);
		
		IAbstractString expectedResult = new StringSequence(
				new StringConstant("a"),
				new StringChoice(
						new StringConstant("b"),
						new StringConstant("")
				)
		);
		
		IAbstractString result = StringConverter.optimizeChoice(startingVersion);
//		System.out.println("START: " + startingVersion);
//		System.out.println("ERES: " + expectedResult);
//		System.out.println("RES: " + result);
		
		assertEquals(expectedResult.toString(), result.toString());
		
	}
	
	@Test
	public void testFlattenstringCollections() {
		IAbstractString nonFlat = new StringSequence(
				new StringSequence(
						new StringConstant("a"),
						new StringConstant("b")
				),
				new StringSequence(
						new StringConstant("d"),
						new StringConstant("e")
				)
			);
		
		IAbstractString flattened = StringConverter.flattenStringCollections(nonFlat); 
		
		IAbstractString flat = new StringSequence(
				new StringConstant("a"),
				new StringConstant("b"),
				new StringConstant("d"),
				new StringConstant("e")
			);
		
//		System.out.println("NONFLAT: " + nonFlat);
//		System.out.println("FLATTENED: " + flattened);
//		System.out.println("FLAT: " + flat);
		
		// TODO string representation is not good for this
		assertEquals(flat.toString(), flattened.toString());
	}

}
