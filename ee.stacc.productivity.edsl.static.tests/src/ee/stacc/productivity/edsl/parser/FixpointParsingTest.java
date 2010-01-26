package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.LRParser;


public class FixpointParsingTest {

	@Test
	public void test() throws Exception {
		
	}
	
	public interface IAbstractStackSet {
		boolean add(IAbstractStack stack);
	}
	
	private IAbstractStackSet getSet(Map<State, IAbstractStackSet> abstractStackSets, State state) {
		IAbstractStackSet set = abstractStackSets.get(state);
		if (set == null) {
			set = null;
			abstractStackSets.put(state, set);
		}
		return set;
	}
	
	private boolean parse (LRParser parser, State initial) {
		Map<State, IAbstractStackSet> abstractStackSets = new HashMap<State, IAbstractStackSet>();
		IAbstractStack initialStack = new SimpleStack(parser.getInitialState());
		getSet(abstractStackSets, initial).add(initialStack);
		
		return true;
	}
}
