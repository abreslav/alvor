package bool.my.programming_language.III.two;

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
 * Date: 12.12.11
 * Time: 17:49
 */

public class TestIIItwo {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{

                //III.2-a0-yes
                {
                        "argumentless()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "function(x, y, z)" +
                                "{" +
                                "return x*y*z;" +
                                "}" +
                                "main(x)" +
                                "{" +
                                "return argumentless()+function(x-1, x, x+1);" +
                                "}",
                        true
                },

                //III.2-a1-no
                {
                        "argumentless()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "function(x, y, z)" +
                                "{" +
                                "return x*y*z;" +
                                "}" +
                                "main(x)" +
                                "{" +
                                "return argumentless()+fonction(x-1, x, x+1);" +
                                "}",
                        false
                },

                //III.2-a2-no
                {
                        "argumentless()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(x)" +
                                "{" +
                                "return argumentless()+function(x-1, x, x+1);" +
                                "}" +
                                "function(x, y, z)" +
                                "{" +
                                "return x*y*z;" +
                                "}",
                        false
                },

                //III.2-a3-no
                {
                        "argumentless()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "function(x, y, z)" +
                                "{" +
                                "return x*y*z;" +
                                "}" +
                                "main(x)" +
                                "{" +
                                "return argumentless(x)+function(x-1, x, x+1);" +
                                "}",
                        false
                },

                //III.2-a4-no
                {
                        "argumentless()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "function(x, y, z)" +
                                "{" +
                                "return x*y*z;" +
                                "}" +
                                "main(x)" +
                                "{" +
                                "return argumentless()+function(x-1, x+1);" +
                                "}",
                        false
                },

                //III.2-a5-yes
                {
                        "argumentless()" +
                                "{" +
                                "return argumentless()+1;" +
                                "}" +
                                "function(x, y, z)" +
                                "{" +
                                "return function(x*y*z, 1, argumentless());" +
                                "}" +
                                "main(x)" +
                                "{" +
                                "return argumentless()+function(x-1, x, x+1);" +
                                "}",
                        true
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
