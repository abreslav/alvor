package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

public class IncomingTransitionsTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void test() throws Exception {
		State automaton = AutomataParser.parse(
				"A - B:x;" +
				"B - C:y;" +
				"C - D:z;");
		IncomingTransitionsInitializer.initializeIncomingTransitions(automaton);
		Queue<State> q = new LinkedList<State>();
		q.offer(automaton);
		while (!q.isEmpty()) {
			State poll = q.poll();
			assertTrue(((Collection) poll.getOutgoingTransitions()).size() <= 1);
			assertTrue(((Collection) poll.getIncomingTransitions()).size() <= 1);
			Iterable<Transition> outgoingTransitions = poll.getOutgoingTransitions();
			for (Transition transition : outgoingTransitions) {
				q.offer(transition.getTo());
			}
		}
		
	}
}
