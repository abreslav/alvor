package bool.my;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.parsing.bool.BooleanLRInterpreter;
import ru.tolmachev.table.builder.TableBuilder;
import ru.tolmachev.table.builder.exceptions.WrongInputFileStructureException;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ִלטענטי
 * Date: 11.12.11
 * Time: 17:22
 */

public class AmbiguousForBoolInterpTest {
    private static final String TABLE_PATH = "trunk\\resources\\ambiguous\\table.txt";

    private BooleanLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"abc", true},
                {"aabbc", true},
                {"abcc", true},
                {"aabbcc", true},
                {"", true},
                {"aaabbbccc", true},
                {"abbbccc", true},
                {"abbbccc", true},
        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new BooleanLRInterpreter(table);
    }

    @Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) throws Exception {
        interpreter.clearStack();
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
