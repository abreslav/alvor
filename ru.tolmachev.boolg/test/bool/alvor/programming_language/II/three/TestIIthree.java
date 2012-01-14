package bool.alvor.programming_language.II.three;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
 * Date: 12.12.11
 * Time: 17:10
 */
public class TestIIthree {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRParser interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                //II.3-a0-yes
                {
                        "f()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return f();" +
                                "}",
                        true
                },

                //II.3-a1-yes
                {
                        "f(x, y)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return f(1, 1);" +
                                "}",
                        true
                },

                //II.3-a2-no
                {
                        "f(x,)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.3-a3-no
                {
                        "f(x y)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.3-a4-no
                {
                        "(x)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.3-a5-no
                {
                        "f(,)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.3-a6-no
                {
                        "f()" +
                                "return 1;" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.3-a7-no
                {
                        "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "f()",
                        false
                }
        };
    }


    @BeforeClass
    public void setUp() throws IOException, WrongInputFileStructureException {
        TableBuilder tableBuilder = new TableBuilder(new File(TABLE_PATH));
        LRParserTable table = tableBuilder.buildTable();
        interpreter = new BooleanLRParser(table);
    }

    @org.testng.annotations.Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) throws Exception {
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
