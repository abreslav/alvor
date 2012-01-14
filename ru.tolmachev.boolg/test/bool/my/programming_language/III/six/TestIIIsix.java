package bool.my.programming_language.III.six;

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
 * Time: 18:56
 */

public class TestIIIsix {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{

                //III.6-a0-no
                {
                        "main(x)" +
                                "{" +
                                "x=1;" +
                                "}",
                        false
                },

                //III.6-b0-yes
                {
                        "main(x)" +
                                "{" +
                                "x=x;" +
                                "if(2+2<4)" +
                                "return 1;" +
                                "else if(2+2>4)" +
                                "{" +
                                "x+x*x;" +
                                "return 2;" +
                                "}" +
                                "else " +
                                "return 0;" +
                                "}",
                        true
                },

                //III.6-b1-no
                {
                        "main(x)" +
                                "{" +
                                "x=x;" +
                                "if(2+2<4)" +
                                "x=4-(2+2);" +
                                "else if(2+2>4)" +
                                "{" +
                                "x+x*x;" +
                                "return 2;" +
                                "}" +
                                "else " +
                                "return 0;" +
                                "}",
                        false
                },

                //III.6-b2-no
                {
                        "main(x)" +
                                "{" +
                                "x=x;" +
                                "if(2+2<4)" +
                                "while(2+2==4)" +
                                "return 1;" +
                                "else if(2+2>4)" +
                                "{" +
                                "x+x*x;" +
                                "return 2;" +
                                "}" +
                                "else " +
                                "return 0;" +
                                "}",
                        false
                },

                //III.6-b3-no
                {
                        "main(x)" +
                                "{" +
                                "x=x;" +
                                "if(2+2<4)" +
                                "return 1;" +
                                "else if(2+2>4)" +
                                "{" +
                                "return 2;" +
                                "x+x*x;" +
                                "}" +
                                "else " +
                                "return 0;" +
                                "}",
                        false
                },

                //III.6-c0-yes
                {
                        "main(x)" +
                                "{" +
                                "if(1)" +
                                "return 1;" +
                                "else " +
                                "return 0;" +
                                "}",
                        true
                },

                //III.6-c1-no
                {
                        "main(x)" +
                                "{" +
                                "if(1)" +
                                "return 1;" +
                                "}",
                        false
                },

                //III.6-c2-no
                {
                        "main(x)" +
                                "{" +
                                "while(1)" +
                                "return 1;" +
                                "}",
                        false
                },
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
