import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.lexer.PLLexerData;
import ru.tolmachev.core.BGState;
import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.parsing.bool.BooleanLRParser;
import ru.tolmachev.parsing.conjunctive.stack.GraphStack;
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
		LRParserTable table = new TableBuilder(new File("resources/programming_language/table.txt")).buildTable();
		ILRParser<GraphStack> parser = new BooleanLRParser(table);
		IStackFactory<GraphStack> factory = new IStackFactory<GraphStack>() {
			
			@Override
			public GraphStack newStack(IParserState state) {
				return new GraphStack((BGState) state);
			}
		};
		LexerData lexerData = PLLexerData.DATA;
		ParserSimulator<GraphStack> simulator = new ParserSimulator<GraphStack>(parser, factory, lexerData );
		String longExample = "main(arg)" +
		"{" +
		"var x, while;" +
		"if(arg!=1) 1; else x=1;" +
		"return 1;" +
		"}";
		List<String> errors = simulator.check(new StringConstant(
				longExample
				));
		for (String error : errors) {
			System.out.println(error);
		}
		System.out.println("Done");
	}
}
