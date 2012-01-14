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
 * Time: 0:58
 */

public class RegularGrammarForConjInterpTest {

    private static final String TABLE_PATH = "trunk\\resources\\regular\\table.txt";

    private ConjunctiveLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"1001", true},
                {"101011001", true},
                {"10101101", false},
                {"10", false},
                {"00", true},
                {"", false},
                {"100", true},
                {"1001", true},
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
