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
 * User: ִלטענטי
 * Date: 27.11.11
 * Time: 22:02
 */

public class ArithmeticGrammarTest {

    private static final String TABLE_PATH = "trunk\\resources\\arithmetic\\table.txt";

    private LRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"1+1", true},
                {"1", true},
                {"-1", true},
                {"(1+1)", true},
                {"(1+1)*2", true},
                {"(19+11)*258", true},
                {"(19+11)*(258-0)", true},
                {"((19+11)*(258-0))/(1-2)", true},
                {"((19+11)*(258-1))/(1-2)", true},
                {"----1", true},
                {"(1+1", false},
                {"1+++1", false},
                {"1/1", true},
        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new LRInterpreter(table);
    }

    @Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) throws Exception {
        interpreter.clearStack();
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }

}
