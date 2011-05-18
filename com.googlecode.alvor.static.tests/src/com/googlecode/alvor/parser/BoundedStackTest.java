package com.googlecode.alvor.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collection;

import org.junit.Test;

import com.googlecode.alvor.sqlparser.BoundedStack;
import com.googlecode.alvor.sqlparser.IAction;
import com.googlecode.alvor.sqlparser.IParserStack;
import com.googlecode.alvor.sqlparser.IParserState;
import com.googlecode.alvor.sqlparser.IStackFactory;


public class BoundedStackTest {
	
	private static final IParserState S1 = new State("S1"); 
	private static final IParserState S2 = new State("S2"); 
	private static final IParserState S3 = new State("S3"); 
	private static final IParserState S4 = new State("S4"); 
	private static final IParserState ERROR = new State("ERROR"); 
	private static final IStackFactory<IParserStack> FACTORY = BoundedStack.getFactory(5, ERROR);
	
	@Test
	public void testCreate() throws Exception {
		IParserStack stack = FACTORY.newStack(S1);
		assertSame(S1, stack.top());
	}
	
	@Test
	public void testPushTop() throws Exception {
		IParserStack res = FACTORY.newStack(S1);
		
		res = res.push(S2);
		assertSame(S2, res.top());
			
		res = res.push(S3);
		assertSame(S3, res.top());

		res = res.push(S1);
		assertSame(S1, res.top());
	}
	
	@Test
	public void testPushPop() throws Exception {
		IParserStack res = FACTORY.newStack(S1);

		// Stack: t -> S1 -> b
		IParserStack pop = res.pop(1);

		res = res.push(S2);
		assertSame(S2, res.top());
		
		// Stack: t -> S2 -> S1 -> b
		pop = res.pop(1);
		
		pop = res.pop(2);
		
		res = res.push(S3);
		assertSame(S3, res.top());
		
		// Stack : t -> S3 -> S2 -> S1 -> b
		pop = res.pop(1);
		

		pop = res.pop(2);
		
		
		pop = res.pop(3);
		
		
		res = res.push(S1);
		assertSame(S1, res.top());
		
		// Stack : t -> S1 -> S3 -> S2 -> S1 -> b
		pop = res.pop(1);
		
		
		pop = res.pop(2);
		
		
		pop = res.pop(3);
		
		
		pop = res.pop(4);
		
		
		res = res.push(S4);
		assertSame(S4, res.top());
		
		// Stack t -> S4 -> S1 -> S2 -> S3 -> S1 -> b
		pop = res.pop(1);
		
		
		pop = res.pop(2);
		
		
		pop = res.pop(3);
		
		
		pop = res.pop(4);
		
		
		res = res.push(S4);
		assertSame(S4, res.top());
		
		// Stack t -> S4 -> S4 -> S1 -> S2 -> S3 -> S1 -> b
		//       t -> S4 -> S1 -> ... 
		pop = res.pop(1);
		
		pop = res.pop(2);
		
		
		pop = res.pop(3);
		
		
		pop = res.pop(4);
		
		
	}
	
	@Test
	public void testOverflow() throws Exception {
		IParserStack res = BoundedStack.getFactory(3, ERROR).newStack(S1);

		// Stack: t -> S1 -> b
		IParserStack pop = res.pop(2);

		res = res.push(S2);
		res = res.push(S3);
		res = res.push(S4);
		assertSame(S4, res.top());

		pop = res.pop(1);
		assertEquals(S3, pop.top());//.iterator().next().top());
		
		pop = res.pop(2);
		assertEquals(S2, pop.top());//.iterator().next().top());
		
		pop = res.pop(3);
		assertEquals(ERROR, pop.top());//.iterator().next().top());
	}
	
	private static final class State implements IParserState {

		private final String text;
		
		public State(String text) {
			this.text = text;
		}

		@Override
		public Collection<IAction> getActions(int symbolNumber) {
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
