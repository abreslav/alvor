package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class AutomataInclusionTest {

	@Test
	public void testInclusion() throws Exception {
		String automaton1;
		String automaton2;

		
		automaton1 = "A - !B:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);

		
		automaton1 = "!A - !B:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		automaton1 = "!A - !A:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
				"!A - B:q C:x;" +
				"B - B:k C:m;" +
				"C - !D:l";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);

		
		automaton1 = "!A - !A:x;";
		automaton2 = 
			"!A - !B:x;" +
			"!B - !B:x";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = "!A1 - !B1:y;";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = 
				"!A1 - !B1:x;" +
				"!B1 - !B1:y";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = 
			"!A1 - !B1:y;" +
			"!B1 - !B1:x";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"A1 - A2:a !A3:a;" +
			"!A2 - A1:b";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"!A1 - !A2:a !A3:a;" +
			"!A2 - !A1:b";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
	}
}