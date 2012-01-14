package bool.my.programming_language.II.one;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
 * Time: 18:49
 */

public class TestIIone {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                //II.1-a0-yes
                {
                        "f(x, y)" +
                                "{" +
                                "return x+y;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "var x;" +
                                "x=f(arg+1, x=1)*-x/(x+arg);" +
                                "return x;" +
                                "}",
                        true},

                //II.1-a1-no
                {
                        "f(x, y)" +
                                "{" +
                                "return x+y;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "var x;" +
                                "x=f(arg+1, x=1)*-x/x+arg);" +
                                "return x;" +
                                "}",
                        false
                },

                //II.1-a2-no
                {
                        "f(x, y)" +
                                "{" +
                                "return x+y;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "var x;" +
                                "x=f(arg+1, x=1)-*x/(x+arg);" +
                                "return x;" +
                                "}",
                        false
                },

                //II.1-a3-no
                {
                        "f(x, y)" +
                                "{" +
                                "return x+y-;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "var x;" +
                                "x=f(arg+1, x=1)*-x/(x+arg);" +
                                "return x;" +
                                "}",
                        false
                },

                //II.1-a4-no
                {
                        "f(x, y)" +
                                "{" +
                                "return x+y;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "var x;" +
                                "x=f(arg+1 x=1)*-x/(x+arg);" +
                                "return x;" +
                                "}",
                        false
                },

                //II.1-a5-no
                {
                        "f(x, y)" +
                                "{" +
                                "return x+y;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "var x;" +
                                "x=f(arg+1, x*2=1)*-x/(x+arg);" +
                                "return x;" +
                                "}",
                        false
                }

        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new BooleanLRInterpreter(table);
    }

    @org.testng.annotations.Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) throws Exception {
        interpreter.clearStack();
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
