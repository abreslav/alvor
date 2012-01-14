package bool.alvor;

import com.sun.org.apache.xpath.internal.operations.Bool;
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
 * Time: 14:14
 */

public class OddPalindromForBoolInterpTest {

    private static final String TABLE_PATH = "trunk\\resources\\odd_polindrom\\table.txt";

    private BooleanLRParser interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"aa", false},
                {"a", true},
                {"b", true},
                {"bb", false},
                {"aba", true},
                {"bbb", true},
                {"bab", true},
                {"abba", false},
                {"aaaa", false},
                {"aaasa", false},
                {"aabaa", true},
                {"ababa", true},
                {"abbba", true},
                {"baaab", true},
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
