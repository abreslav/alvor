package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IAction;
import ee.stacc.productivity.edsl.sqlparser.IParserState;


public class SimpleFoldedStackTest {
	
	private static final IParserState S1 = new State("S1"); 
	private static final IParserState S2 = new State("S2"); 
	private static final IParserState S3 = new State("S3"); 
	private static final IParserState S4 = new State("S4"); 
	
	@Test
	public void testCreate() throws Exception {
		SimpleFoldedStack stack = new SimpleFoldedStack(S1);
		assertSame(S1, stack.top());
	}
	
	@Test
	public void testPushTop() throws Exception {
		IAbstractStack res = new SimpleFoldedStack(S1);
		
		res = res.push(S2);
		assertSame(S2, res.top());
			
		res = res.push(S3);
		assertSame(S3, res.top());

		res = res.push(S1);
		assertSame(S1, res.top());
	}
	
	@Test
	public void testPushPop() throws Exception {
		IAbstractStack res = new SimpleFoldedStack(S1);

		// Stack: t -> S1 -> b
		Set<IAbstractStack> pop = res.pop(1);
		assertTrue(pop.isEmpty()); // Maybe it should be a single empty stack...

		pop = res.pop(2);
		assertTrue(pop.isEmpty());
		
		res = res.push(S2);
		assertSame(S2, res.top());
		
		// Stack: t -> S2 -> S1 -> b
		pop = res.pop(1);
		assertEquals(1, pop.size());
		
		pop = res.pop(2);
		assertEquals(0, pop.size());
		
		res = res.push(S3);
		assertSame(S3, res.top());
		
		// Stack : t -> S3 -> S2 -> S1 -> b
		pop = res.pop(1);
		assertEquals(1, pop.size());

		pop = res.pop(2);
		assertEquals(1, pop.size());
		
		pop = res.pop(3);
		assertEquals(0, pop.size());
		
		res = res.push(S1);
		assertSame(S1, res.top());
		
		// Stack : t -> S1 -> S3 -> S2 -> S1 -> b
		pop = res.pop(1);
		assertEquals(1, pop.size());
		
		pop = res.pop(2);
		assertEquals(1, pop.size());
		
		pop = res.pop(3);
		assertEquals(1, pop.size());
		
		pop = res.pop(4);
		assertEquals(1, pop.size());
		
		pop = res.pop(100);
		assertEquals(1, pop.size());

		res = res.push(S4);
		assertSame(S4, res.top());
		
		// Stack t -> S4 -> S1 -> S2 -> S3 -> S1 -> b
		pop = res.pop(1);
		assertEquals(1, pop.size());
		
		pop = res.pop(2);
		assertEquals(1, pop.size());
		
		pop = res.pop(3);
		assertEquals(1, pop.size());
		
		pop = res.pop(4);
		assertEquals(1, pop.size());
		
		pop = res.pop(100);
		assertEquals(1, pop.size());

		res = res.push(S4);
		assertSame(S4, res.top());
		
		// Stack t -> S4 -> S4 -> S1 -> S2 -> S3 -> S1 -> b
		//       t -> S4 -> S1 -> ... 
		pop = res.pop(1);
		assertEquals(2, pop.size());
		
		pop = res.pop(2);
		assertEquals(3, pop.size());
		
		pop = res.pop(3);
		assertEquals(4, pop.size());
		
		pop = res.pop(4);
		assertEquals(4, pop.size());
		
		pop = res.pop(100);
		assertEquals(4, pop.size());
		
	}
	
	private static final class State implements IParserState {

		private final String text;
		
		public State(String text) {
			this.text = text;
		}

		@Override
		public IAction getAction(int symbolNumber) {
			return null;
		}

		@Override
		public boolean isTerminating() {
			return false;
		}

		@Override
		public String toString() {
			return text;
		}

		@Override
		public boolean isError() {
			return false;
		}
		
	}
}
