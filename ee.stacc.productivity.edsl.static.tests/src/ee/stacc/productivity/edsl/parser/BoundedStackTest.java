package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.BoundedStack;
import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IAction;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.IStackFactory;


public class BoundedStackTest {
	
	private static final IParserState S1 = new State("S1"); 
	private static final IParserState S2 = new State("S2"); 
	private static final IParserState S3 = new State("S3"); 
	private static final IParserState S4 = new State("S4"); 
	private static final IParserState ERROR = new State("ERROR"); 
	private static final IStackFactory FACTORY = BoundedStack.getFactory(5, ERROR);
	
	@Test
	public void testCreate() throws Exception {
		IAbstractStack stack = FACTORY.newStack(S1);
		assertSame(S1, stack.top());
	}
	
	@Test
	public void testPushTop() throws Exception {
		IAbstractStack res = FACTORY.newStack(S1);
		
		res = res.push(S2);
		assertSame(S2, res.top());
			
		res = res.push(S3);
		assertSame(S3, res.top());

		res = res.push(S1);
		assertSame(S1, res.top());
	}
	
	@Test
	public void testPushPop() throws Exception {
		IAbstractStack res = FACTORY.newStack(S1);

		// Stack: t -> S1 -> b
		Set<IAbstractStack> pop = res.pop(1);
		assertTrue(pop.toString(), pop.size() == 1); // Maybe it should be a single empty stack...

		res = res.push(S2);
		assertSame(S2, res.top());
		
		// Stack: t -> S2 -> S1 -> b
		pop = res.pop(1);
		assertEquals(1, pop.size());
		
		pop = res.pop(2);
		assertEquals(1, pop.size());
		
		res = res.push(S3);
		assertSame(S3, res.top());
		
		// Stack : t -> S3 -> S2 -> S1 -> b
		pop = res.pop(1);
		assertEquals(1, pop.size());

		pop = res.pop(2);
		assertEquals(1, pop.size());
		
		pop = res.pop(3);
		assertEquals(1, pop.size());
		
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
		
		res = res.push(S4);
		assertSame(S4, res.top());
		
		// Stack t -> S4 -> S4 -> S1 -> S2 -> S3 -> S1 -> b
		//       t -> S4 -> S1 -> ... 
		pop = res.pop(1);
		assertEquals(pop.toString(), 1, pop.size());
		
		pop = res.pop(2);
		assertEquals(1, pop.size());
		
		pop = res.pop(3);
		assertEquals(1, pop.size());
		
		pop = res.pop(4);
		assertEquals(1, pop.size());
		
	}
	
	@Test
	public void testOverflow() throws Exception {
		IAbstractStack res = BoundedStack.getFactory(3, ERROR).newStack(S1);

		// Stack: t -> S1 -> b
		Set<IAbstractStack> pop = res.pop(2);
		assertTrue(pop.toString(), pop.size() == 1); // Maybe it should be a single empty stack...

		res = res.push(S2);
		res = res.push(S3);
		res = res.push(S4);
		assertSame(S4, res.top());

		pop = res.pop(1);
		assertEquals(S3, pop.iterator().next().top());
		
		pop = res.pop(2);
		assertEquals(S2, pop.iterator().next().top());
		
		pop = res.pop(3);
		assertEquals(ERROR, pop.iterator().next().top());
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
