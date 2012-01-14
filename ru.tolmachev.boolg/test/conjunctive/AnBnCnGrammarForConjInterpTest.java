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
 * Time: 1:45
 */

public class AnBnCnGrammarForConjInterpTest {
    private static final String TABLE_PATH = "trunk\\resources\\anbncn\\table.txt";

    private ConjunctiveLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"", true},
                {"abc", true},
                {"aabbcc", true},
                {"aabbc", false},
                {"aabc", false},
                {"abbc", false},
                {"abcc", false},
                {"abbcc", false},
                {"aaabbbccc", true},
                {"aaaabbbbcccc", true},
                {"aaaaabbbbbccccc", true},
                {"aaaaaabbbbbbcccccc", true},
                {"aaaaaabbbbbbccccc", false},
                {"aaaaabbbbbbccccc", false},
                {"aaaaaabbbbbccccc", false},
        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new ConjunctiveLRInterpreter(table);
    }

    @Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) throws Exception {
        interpreter.clearStack();
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
