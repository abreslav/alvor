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
 * Date: 06.12.11
 * Time: 0:50
 */

public class SimpleRegularForBoolInterprTest {
    private static final String TABLE_PATH = "trunk\\resources\\simple_regular\\table.txt";

    private BooleanLRParser interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"", true},
                {"a", true},
                {"aa", true},
                {"aaa", true},
                {"aaaa", true},
                {"aaaaa", true},
                {"aaaaaa", true},
                {"aaaaaaa", true},
                {"aba", false},
                {"abb", false},
                {"abba", false},
                {"abbab", false},
        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new BooleanLRParser(table);
    }

    @Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) {
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
