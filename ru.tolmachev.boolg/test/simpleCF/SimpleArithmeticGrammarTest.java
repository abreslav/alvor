package simpleCF;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.parsing.simple_cf.LRInterpreter;
import ru.tolmachev.table.builder.TableBuilder;
import ru.tolmachev.table.builder.exceptions.WrongInputFileStructureException;


import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 27.11.11
 * Time: 19:05
 */

public class SimpleArithmeticGrammarTest {

    private static final String TABLE_PATH = "trunk\\resources\\simple_arithmetic\\table.txt";

    private LRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"1+1", true},
                {"1", true},
                {"0", true},
                {"0+0", true},
                {"1+1+1", true},
                {"1*1*1*0", true},
                {"1++", false},
                {"+0+", false},
                {"1++0", false},
                {"10", false},
                {"1+0*1+1*1+0*1", true},
                {"1*1+1+1*0+1*1*1+1+1", true}
        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new LRInterpreter(table);
    }

    @Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) {
        interpreter.clearStack();
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }

}
