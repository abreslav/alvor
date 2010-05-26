package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractOutputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence;
import ee.stacc.productivity.edsl.lexer.alphabet.SimpleCharacter;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexer;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;
import ee.stacc.productivity.edsl.tests.util.TestUtil;

public class AutomataInclusionTest {

	@Test
	public void testInclusion() throws Exception {
		String automaton1;
		String automaton2;


		automaton1 = "!A - !A:x;";
		automaton2 = "!A1 - !B1:x;" + "!B1 - !B1:y";
		assertFalse(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "A - !B:q;";
		automaton2 = automaton1;
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "!A - !B:q;";
		automaton2 = automaton1;
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "!A - !A:q;";
		automaton2 = automaton1;
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "!A - B:q C:x;" + "B - B:k C:m;" + "C - !D:l";
		automaton2 = automaton1;
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "!A - !A:x;";
		automaton2 = "!A - !B:x;" + "!B - !B:x";
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "!A - !A:x;";
		automaton2 = "!A1 - !B1:y;";
		assertFalse(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "!A - !A:x;";
		automaton2 = "!A1 - !B1:y;" + "!B1 - !B1:x";
		assertFalse(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "S1 - !S2:a;" + "!S2 - S3:b;" + "S3 - S2:a";
		automaton2 = "A1 - A2:a !A3:a;" + "!A2 - A1:b";
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "S1 - !S2:a;" + "!S2 - S3:b;" + "S3 - S2:a";
		automaton2 = "!A1 - !A2:a !A3:a;" + "!A2 - !A1:b";
		assertFalse(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "S1 - !S2:a S1:x;" + "!S2 - S3:b;" + "S3 - S2:a";
		automaton2 = "A1 - A2:a !A3:a;" + "!A2 - A1:b";
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "S1 - !S2:a;" + "!S2 - S3:b;" + "S3 - S2:a";
		automaton2 = "A1 - A2:a !A3:a A1:x;" + "!A2 - A1:b";
		assertFalse(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		// aa*
		automaton1 = "A1 - !A2:a;" + "!A2 - !A2:a A3:b";
		// aaa*
		automaton2 = "S1 - S2:a;" + "S2 - !S3:a;" + "!S3 - !S3:a;";
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		// VV*SS*V | SS*V | V
		automaton1 = "C1 - !C13:V C2:S;" + "C2 - C2:S !C3:V;"
				+ "!C13 - !C13:V C6:S;" + "C6 - C6:S !C7:V;";
		automaton2 = "A1 - !A3:V A4:S;" + "!A3 - !A3:V A4:S;"
				+ "A4 - A4:S !A5:V;";
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1), AutomataParser.parse(
				automaton2)));

		automaton1 = "C1 - C1:V C2:S !C3:V;" + "C2 - C2:S !C3:V;";
		automaton2 = "A1 - A4:S !A5:V A1:V;" + "A4 - A4:S !A5:V;" + "!A5;";
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(
				AutomataDeterminator.determinate(AutomataParser.parse(
						automaton1)), AutomataDeterminator
						.determinate(AutomataParser.parse(automaton2)
								)));

		automaton1 = "C1 - !C13:V C2:S;" + "C2 - C2:S !C3:V;"
				+ "!C13 - !C13:V C6:S;" + "C6 - C6:S !C7:V;";
		automaton2 = "A1 - A4:S !A5:V A1:V;" + "A4 - A4:S !A5:V;" + "!A5;";
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(AutomataParser
				.parse(automaton1),
				AutomataDeterminator.determinate(AutomataParser.parse(
						automaton2))));

	}

	@Test
	public void testTransduction() throws Exception {
		String automatonStr;
		String transducerStr;
		String checkStr;

		automatonStr = 
			"A1 - !A2:a;" + 
			"!A2 - A3:b;" + 
			"A3 - A2:a";
		transducerStr = 
			"T1 - !T2:a/x;" + 
			"!T2 - T3:b/y;" + 
			"T3 - T2:a/z";
		checkStr = 
			"C1 - !C2:x;" + 
			"!C2 - C3:y;" + 
			"C3 - C2:z";

		checkTransduction(automatonStr, transducerStr, checkStr);

		// 0*_+("")*0
		automatonStr = 
			"A1 - A1:0 A2:_;" + 
			"A2 - A2:_ A3:\" !A5:0;" + 
			"A3 - A4:\";" + 
			"A4 - A3:\" !A5:0";
		// (0+/V | "a*"/S | _)*
		transducerStr = 
			"!T1 - !T1:_ !T2:0/V T3:\" ;" + 
			"!T2 - !T2:0 !T1:_ T3:\" ;" + 
			"T3 - T3:a !T1:\"/S ;";
		// V*S*V
		checkStr = 
			"C1 - C1:V C2:S !C3:V;" + 
			"C2 - C2:S !C3:V;";

		checkTransduction(automatonStr, transducerStr, checkStr);
	}

	private void checkTransduction(String automatonStr, String transducerStr,
			String checkStr) {
		State automaton = AutomataParser.parse(automatonStr);
		automaton = AutomataDeterminator.determinate(automaton);
		State transducer = AutomataParser.parse(transducerStr);
		State check = AutomataParser.parse(checkStr);
		check = AutomataDeterminator.determinate(check);
		
		State transduction = AutomataInclusion.INSTANCE.getTrasduction(transducer, automaton, IAlphabetConverter.ID, new IOutputItemInterpreter() {

			@Override
			public ISequence<IAbstractInputItem> processOutputCommands(
					ISequence<IAbstractInputItem> text,
					IAbstractInputItem inputItem,
					List<IAbstractOutputItem> output,
					List<IAbstractInputItem> effect) {
				for (IAbstractOutputItem command : output) {
					char outChar = ((SimpleOutput) command).getOutChar();
					effect.add(SimpleCharacter.create(outChar));
				}
				return text;
			}
			
		});
		transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction);
		transduction = AutomataDeterminator.determinate(transduction);
//		AutomataUtils.printAutomaton(automaton);
//		AutomataUtils.printAutomaton(check);
//		AutomataUtils.printAutomaton(transduction);
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(check, transduction));
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(transduction, check));
	}

	@Test
	public void testSQL() throws Exception {
		String[] strings = {
				"SELECT cc.ColumnName FROM AD_Column c",
				"SELECT t.TableName FROM AD_Column c",
				"SELECT AD_Window_ID, IsReadOnly FROM AD_Menu WHERE AD_Menu_ID=? AND Action='W'",
				"SELECT GRANTOR,GRANTEE,DBADMAUTH FROM SYSCAT.DBAUTH",
				"INSERT INTO X_Test(Text1, Text2) values(?,?)",
				"SELECT * FROM AD_System",
				"INSERT INTO X_Test(Text1, Text2) values(?,?)",
				"SELECT c.ColumnName FROM AD_Column c INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) ",
				"SELECT AD_Table_ID, TableName FROM AD_Table WHERE IsView='N' ORDER BY 2",
				"SELECT COUNT(*) FROM AD_PInstance_Para WHERE AD_PInstance_ID=?", };
		State automaton = AutomataUtils
				.toAutomaton(new HashSet<String>(Arrays
						.asList(strings)));

		String[] expected = {
			"SELECT ID . ID FROM ID ID",
			"SELECT ID . ID FROM ID ID",
			"SELECT ID , ID FROM ID WHERE ID = ? AND ID = STRING_SQ",
			"SELECT ID , ID , ID FROM ID . ID",
			"INSERT INTO ID(ID, ID) VALUES(?,?)",
			"SELECT * FROM ID",
			"INSERT INTO ID(ID, ID) VALUES(?,?)",
			"SELECT ID.ID FROM ID ID INNER JOIN ID ID ON (ID.ID=ID.ID) ",
			"SELECT ID, ID FROM ID WHERE ID=STRING_SQ ORDER BY NUMBER",
			"SELECT ID(*) FROM ID WHERE ID=?",
		};
		checkAutomatonTransduction(expected, automaton);
	}


	private void checkAutomatonTransduction(String[] expected, State init) {
		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		State transduction = AutomataInclusion.INSTANCE.getTrasduction(
				sqlTransducer, init, SQLLexer.SQL_ALPHABET_CONVERTER);
		transduction = EmptyTransitionEliminator.INSTANCE
				.eliminateEmptySetTransitions(transduction);
//		AutomataUtils.generate(transduction, AutomataUtils.SQL_TOKEN_TO_STRING, AutomataUtils.STANDARD_OUTPUT);
		transduction = AutomataDeterminator.determinate(transduction);

		TestUtil.checkGeneratedSQLStrings(transduction, expected);
	}

	@Test
	public void testLoops() throws Exception {
//		fail("Loops are not supported");
		
		String abstractString;
		String[] expected;

		abstractString = "([1234567890])+";
		expected = new String[] {
				"NUMBER",	
		};
		checkAbstractStringTransduction(AbstractStringParser.parseOneFromString(abstractString), expected);

		abstractString = "\"SELECT \" ([a1])+ \" FROM\"";
		expected = new String[] {
				"SELECT ID FROM",	
				"SELECT NUMBER FROM",	
				"SELECT DIGAL_ERR FROM",	
		};
		checkAbstractStringTransduction(AbstractStringParser.parseOneFromString(abstractString), expected);

		abstractString = "\"SELECT \" \"?\" {(\",\" \"?\")+, } \" FROM\"";
		expected = new String[] {
				"SELECT ? FROM",	
				"SELECT ? , ? FROM",	
		};
		checkAbstractStringTransduction(AbstractStringParser.parseOneFromString(abstractString), expected);
		
		abstractString = "\"SELECT \" (\"?\" {\",\", })+ \" FROM\"";
		expected = new String[] {
				"SELECT ? FROM",	
				"SELECT ? , ? FROM",	
				"SELECT ? , FROM",	
				"SELECT ? ? , FROM",	
				"SELECT ? ? , ? FROM",	
				"SELECT ? , ? ? FROM",	
				"SELECT ? ? FROM",	
		};
		checkAbstractStringTransduction(AbstractStringParser.parseOneFromString(abstractString), expected);
		
		
		abstractString = "\"SELECT \" ([a.])+ \" FROM\"";
		expected = new String[] {
				"SELECT  ID  FROM  ",
				"SELECT  ID . ID  FROM  ",
				"SELECT  ID .  FROM  ",
				"SELECT  ID . . ID  FROM  ",
				"SELECT  ID . .  FROM  ",
				"SELECT   FROM  ",
				"SELECT  . ID  FROM  ",
				"SELECT  . ID .  FROM  ",
				"SELECT  . ID . .  FROM  ",
				"SELECT  .  FROM  ",
				"SELECT  . . ID  FROM  ",
				"SELECT  . . ID .  FROM  ",
				"SELECT  . .  FROM  ",
		};
		checkAbstractStringTransduction(AbstractStringParser.parseOneFromString(abstractString), expected);
		
		
		abstractString = "\"SELECT \" ([0123456789,])+ \" FROM\"";
		expected = new String[] {
				"SELECT  NUMBER  FROM  ",
				"SELECT  NUMBER , NUMBER  FROM  ",
				"SELECT  NUMBER ,  FROM  ",
				"SELECT  NUMBER , , NUMBER  FROM  ",
				"SELECT  NUMBER , ,  FROM  ",
				"SELECT   FROM  ",
				"SELECT  , NUMBER  FROM  ",
				"SELECT  , NUMBER ,  FROM  ",
				"SELECT  , NUMBER , ,  FROM  ",
				"SELECT  ,  FROM  ",
				"SELECT  , , NUMBER  FROM  ",
				"SELECT  , , NUMBER ,  FROM  ",
				"SELECT  , ,  FROM  ",
		};
		checkAbstractStringTransduction(AbstractStringParser.parseOneFromString(abstractString), expected);
		
	}

	private void checkAbstractStringTransduction(IAbstractString str,
			String[] expected) {
		State init = StringToAutomatonConverter.INSTANCE.convert(str);
//		init = AutomataDeterminator.determinate(init);

		checkAutomatonTransduction(expected, init);
	}
	
//	private Set<String> convertToSQLChars(Set<String> hashSet) {
//		Set<String> result = new HashSet<String>();
//		for (String string : hashSet) {
//			StringBuilder builder = new StringBuilder(string.length());
//			for (int i = 0; i < string.length(); i++) {
//				builder.append(SQLLexerData.CHAR_CLASSES[string.charAt(i)]);
//			}
//			String res1 = builder.toString();
//			String res = res1;
//			result.add(res);
//		}
//		return result;
//	}

}