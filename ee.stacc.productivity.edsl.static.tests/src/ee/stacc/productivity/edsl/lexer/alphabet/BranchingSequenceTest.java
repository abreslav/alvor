package ee.stacc.productivity.edsl.lexer.alphabet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.alphabet.ISequence.IFoldFunction;


public class BranchingSequenceTest {

	private ISequence<Integer> create() {
		return new BranchingSequence<Integer>();
	}
	
	private ISequence<Integer> create(Integer x) {
		return new BranchingSequence<Integer>(x);
	}
	
	@Test
	public void testIsEmpty() throws Exception {
		assertTrue(create().isEmpty());
		assertFalse(create(1).isEmpty());
	}
	
	@Test
	public void testFoldEmpty() throws Exception {
		assertEquals(Integer.valueOf(1), create().fold(1, null));
	}
	
	@Test
	public void testFoldOne() throws Exception {
		assertEquals("1", create(1).fold("", Concat.INSTANCE));
	}
	
	@Test
	public void testFoldMany() throws Exception {
		ISequence<Integer> seq = 
 			 create(1)
			.append(2)
			.append(3)
			.append(4)
			.append(5)
			;
		assertEquals("1 2 3 4 5", seq.fold("", Concat.INSTANCE));
	}
	
	@Test
	public void testWithSet() throws Exception {
		ISequence<Integer> seq = 
			 create(1)
			.append(2)
			.append(3)
			.append(4)
			.append(5)
			;
		ISequence<Integer> seq1 = 
			 create(1)
			.append(2)
			.append(3)
			.append(4)
			.append(5)
			;
		ISequence<Integer> seq2 = 
			 create(1)
			.append(2)
			.append(3)
			.append(4)
			.append(5)
			;
		ISequence<Integer> seq3 = 
			create(1)
			.append(2)
			.append(3)
			.append(3)
			.append(5)
			;
		HashSet<ISequence<Integer>> set = new HashSet<ISequence<Integer>>();
		set.add(seq);
		set.add(seq1);
		set.add(seq2);
		set.add(seq3);
		assertEquals(2, set.size());
		HashSet<ISequence<Integer>> set1 = new HashSet<ISequence<Integer>>();
		set1.add(seq);
		set1.add(seq3);
		assertEquals(set, set1);
	}
	
	private static class Concat implements IFoldFunction<String, Integer> {

		public static final Concat INSTANCE = new Concat();
		
		@Override
		public String body(String init, Integer arg, boolean last) {
			return init + arg + (last ? "" : " ");
		}
		
	}
	
}
