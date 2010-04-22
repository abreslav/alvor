package ee.stacc.productivity.edsl.crawler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringSequence;


public class StringConverterTest {
	
	@Test
	public void testEArvedStrings() {
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
		
		System.out.println("NONFLAT: " + nonFlat);
		System.out.println("FLATTENED: " + flattened);
		System.out.println("FLAT: " + flat);
		
		// TODO string representation is not good for this
		assertEquals(flat.toString(), flattened.toString());
	}

}
