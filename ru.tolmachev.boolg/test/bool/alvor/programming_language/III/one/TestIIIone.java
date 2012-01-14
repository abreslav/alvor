package bool.alvor.programming_language.III.one;

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
 * Time: 17:28
 */


public class TestIIIone {
    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRParser interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{

                //III.1-a0-yes
                {
                        "function()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "fonction(x, y)" +
                                "{" +
                                "return 1;" +
                                "}",
                        true
                },

                //III.1-a1-no
                {
                        "function()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "function(x, y)" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //III.1-a2-no
                {
                        "function()" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "function()" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //III.1-b0-yes
                {
                        "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
                        true
                },

                //III.1-b1-no
                {
                        "main(first, second)" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //III.1-b2-no
                {
                        "principale(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
                        false
                },

                //III.1-c0-no
                {
                        "",
                        false
                },
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
