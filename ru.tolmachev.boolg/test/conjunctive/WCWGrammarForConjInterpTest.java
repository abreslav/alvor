package conjunctive;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.parsing.conjunctive.ConjunctiveLRInterpreter;
import ru.tolmachev.table.builder.TableBuilder;
import ru.tolmachev.table.builder.exceptions.WrongInputFileStructureException;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ִלטענטי
 * Date: 06.12.11
 * Time: 10:25
 */

public class WCWGrammarForConjInterpTest {

    private static final String TABLE_PATH = "trunk\\resources\\wcw\\table.txt";

    private ConjunctiveLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"aca", true},
                {"bcb", true},
                {"abcab", true},
                {"bacba", true},
                {"aacaa", true},
                {"bbcbb", true},
                {"aaacaaa", true},
                {"aabcaab", true},
                {"abacaba", true},
                {"baacbaa", true},
                {"bbacbba", true},
                {"abbcabb", true},
                {"bbacbba", true},
                {"babcbab", true},
                {"bbbcbbb", true},
                {"bbbcbba", false},
                {"babcbba", false},
                {"aabcbba", false},
                {"aabcbaa", false},
                {"abcaa", false},
                {"abcba", false},
                {"abcbb", false},
                {"bbcab", false},
                {"acb", false},
                {"bca", false},
                {"ddd", false},
                {"ccc", false},
                {"aaa", false},
                {"aab", false},
                {"", false},
                {"c", true},
        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new ConjunctiveLRInterpreter(table);
    }

    @Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) {
        interpreter.clearStack();
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
