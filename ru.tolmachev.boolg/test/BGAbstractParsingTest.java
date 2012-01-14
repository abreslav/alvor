import java.io.File;
import java.io.IOException;
import java.util.List;

import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.parsing.bool.BooleanLRParser;
import ru.tolmachev.parsing.conjunctive.stack.GraphStack;
import ru.tolmachev.parsing.conjunctive.stack.GraphStackNode;
import ru.tolmachev.table.builder.TableBuilder;
import ru.tolmachev.table.builder.exceptions.WrongInputFileStructureException;

import com.googlecode.alvor.lexer.automata.LexerData;
import com.googlecode.alvor.sqlparser.ILRParser;
import com.googlecode.alvor.sqlparser.IParserState;
import com.googlecode.alvor.sqlparser.IStackFactory;
import com.googlecode.alvor.sqlparser.ParserSimulator;
import com.googlecode.alvor.string.StringConstant;

import junit.framework.TestCase;
import lexer.BGLexerData;


public class BGAbstractParsingTest extends TestCase {
	public void testSimplest() throws Exception {
		String dataDir = "simplest";
		doTest(dataDir, "a", true);
		doTest(dataDir, "b", false);
		doTest(dataDir, "", false);
		doTest(dataDir, "aa", false);
		doTest(dataDir, "ab", false);
	}

	public void testSimple_ab() throws Exception {
		String dataDir = "simple_ab";
		doTest(dataDir, "ab", true);
		doTest(dataDir, "", false);
		doTest(dataDir, "a", false);
		doTest(dataDir, "b", false);
		doTest(dataDir, "ba", false);
		doTest(dataDir, "aa", false);
		doTest(dataDir, "aba", false);
	}
	
	public void testSimple_PL() throws Exception {
		String dataDir = "programming_language";
		doTest(dataDir, "main(arg) {return 1;}", true);
		doTest(dataDir, "main(arg) {var a; a = 1; return a;}", true);
		doTest(dataDir, "main(arg) {var a; a = 1; return b;}", false);
		doTest(dataDir, "main(arg) {var a; b = 1; return a;}", false);
		doTest(dataDir, "", false);
	}
	
	private static void doTest(String dataDir, String example, boolean correct) throws WrongInputFileStructureException, IOException {
		LRParserTable table = new TableBuilder(new File("resources/" + dataDir + "/table.txt")).buildTable();
		ILRParser<GraphStack> parser = new BooleanLRParser(table);
		IStackFactory<GraphStack> factory = new IStackFactory<GraphStack>() {
			
			@Override
			public GraphStack newStack(IParserState state) {
				return new GraphStack((GraphStackNode) state);
			}
		};
		LexerData lexerData = BGLexerData.DATA;
		ParserSimulator<GraphStack> simulator = new ParserSimulator<GraphStack>(parser, factory, lexerData);
		StringBuilder report = new StringBuilder("\nTesting: " + example + "\n");
		List<String> errors = simulator.check(new StringConstant(example));
		for (String error : errors) {
			report.append(error).append("\n");
		}
		report.append("Errors found: " + errors.size()).append("\n");
		assertEquals(report.toString(), correct, errors.isEmpty());
	}

}
