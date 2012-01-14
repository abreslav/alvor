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
 * Time: 1:04
 */

public class ArithmeticGrammarForBoolInterpTest {

    private static final String TABLE_PATH = "trunk\\resources\\arithmetic\\table.txt";

    private BooleanLRParser interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                {"1+1", true},
                {"1", true},
                {"-1", true},
                {"(1+1)", true},
                {"(1+1+2+89)", true},
                {"(1+1+2+89)*56*67", true},
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
        interpreter = new BooleanLRParser(table);
    }

    @Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) throws Exception {
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
