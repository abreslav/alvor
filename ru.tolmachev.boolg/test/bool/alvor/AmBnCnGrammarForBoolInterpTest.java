package bool.alvor;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.parsing.bool.BooleanLRInterpreter;
import ru.tolmachev.parsing.bool.BooleanLRParser;
import ru.tolmachev.table.builder.TableBuilder;
import ru.tolmachev.table.builder.exceptions.WrongInputFileStructureException;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ִלטענטי
 * Date: 10.12.11
 * Time: 20:51
 */

public class AmBnCnGrammarForBoolInterpTest {

    private static final String TABLE_PATH = "trunk\\resources\\ambncn\\table.txt";

    private BooleanLRParser interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"abc", false},
                {"aabbcc", false},
                {"aaabbbccc", false},
                {"aabbccc", false},
                {"a", true},
                {"abbcc", true},
                {"abbbccc", true},
                {"aabbbccc", true},
                {"aaabbbccc", false},
                {"aaaabbbccc", true},
                {"aaaaabbbccc", true},
                {"abbbccc", true},
                {"aabbbccc", true},
                {"aaaabbbbcccc", false},
        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new BooleanLRParser(table);
    }

    @Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) throws Exception {
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
