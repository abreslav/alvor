import java.io.File;
import java.io.IOException;
import java.util.List;

import lexer.BGLexerData;
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


public class SimpleAbstractParsingTest {
	public static void main(String[] args) throws WrongInputFileStructureException, IOException {
		String example = 
				"main(arg)" +
				"{" +
					"var x, while;" +
					"if(arg!=1) 1; else x=1;" +
					"return 1;" +
				"}";
		boolean ok = true;
		ok &= test("a", false);
		ok &= test("ab", true);
		ok &= test("b", false);
		ok &= test("aba", false);
		ok &= test("aa", false);
		System.out.println("===");
		System.out.println(ok ? "OK" : "FAIL");
	}

	private static boolean test(String example, boolean correct) throws WrongInputFileStructureException, IOException {
		LRParserTable table = new TableBuilder(new File("resources/simple_ab/table.txt")).buildTable();
		ILRParser<GraphStack> parser = new BooleanLRParser(table);
		IStackFactory<GraphStack> factory = new IStackFactory<GraphStack>() {
			
			@Override
			public GraphStack newStack(IParserState state) {
				return new GraphStack((GraphStackNode) state);
			}
		};
		LexerData lexerData = BGLexerData.DATA;
		ParserSimulator<GraphStack> simulator = new ParserSimulator<GraphStack>(parser, factory, lexerData);
		System.out.println("Testing: " + example);
		List<String> errors = simulator.check(new StringConstant(example));
		for (String error : errors) {
			System.out.println(error);
		}
		System.out.println("Errors found: " + errors.size());
		boolean result = correct == errors.isEmpty();
		System.out.println(result ? "OK" : "Fail");
		return result;
	}
}
