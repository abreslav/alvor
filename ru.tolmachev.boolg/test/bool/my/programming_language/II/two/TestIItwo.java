package bool.my.programming_language.II.two;

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
 * Time: 18:59
 */

public class TestIItwo {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                //II.2-a0-yes
                {
                        "main(arg)" +
                                "{" +
                                "var x;" +
                                "if(x>x)" +
                                "while(x==x)" +
                                "x=x;" +
                                "else" +
                                "{" +
                                "if(x==arg)" +
                                "return x;" +
                                "{ }" +
                                "x;" +
                                "}" +
                                "return x;" +
                                "}",
                        true},

                //II.2-b0-yes
                {
                        "main(arg)" +
                                "{" +
                                "arg=arg;" +
                                "return arg;" +
                                "}",
                        true
                },

                //II.2-b1-no
                {
                        "main(arg)" +
                                "{" +
                                "arg=arg" +
                                "return arg;" +
                                "}",
                        false
                },

                //II.2-b2-no
                {
                        "main(arg)" +
                                "{" +
                                ";" +
                                "return arg;" +
                                "}",
                        false
                },

                //II.2-b3-yes
                {
                        "main(arg)" +
                                "{" +
                                "{ arg=arg; }" +
                                "return arg;" +
                                "}",
                        true
                },

                //II.2-b4-no
                {
                        "main(arg)" +
                                "{" +
                                "{ arg=arg;" +
                                "return arg;" +
                                "}",
                        false
                },

                //II.2-b5-yes
                {
                        "main(arg)" +
                                "{" +
                                "var x;" +
                                "arg=arg;" +
                                "return arg;" +
                                "}",
                        true
                },

                //II.2-b6-no
                {
                        "main(arg)" +
                                "{" +
                                "var ;" +
                                "arg=arg;" +
                                "return arg;" +
                                "}",
                        false
                },

                //II.2-c0-yes
                {
                        "main(arg)" +
                                "{" +
                                "if(1)" +
                                "arg=arg;" +
                                "return 1;" +
                                "}",
                        true
                },

                //II.2-c1-yes
                {
                        "main(arg)" +
                                "{" +
                                "if(1)" +
                                "arg=arg;" +
                                "else " +
                                "arg=-arg;" +
                                "return 1;" +
                                "}",
                        true
                },

                //II.2-c2-no
                {
                        "main(arg)" +
                                "{" +
                                "if(1)" +
                                "arg=arg;" +
                                "else " +
                                "arg=-arg;" +
                                "else " +
                                "arg=arg+arg;" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.2-c3-no
                {
                        "main(arg)" +
                                "{" +
                                "else" +
                                "arg=arg;" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.2-c4-no
                {
                        "main(arg)" +
                                "{" +
                                "if 1" +
                                "arg=arg;" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.2-c5-yes
                {
                        "main(arg)" +
                                "{" +
                                "while(1)" +
                                "arg=arg;" +
                                "return 1;" +
                                "}",
                        true
                },

                //II.2-c6-no
                {
                        "main(arg)" +
                                "{" +
                                "while(1)" +
                                "arg=arg;" +
                                "else " +
                                "1;" +
                                "return 1;" +
                                "}",
                        false
                },

                //II.2-d0-yes
                {
                        "main(arg)" +
                                "{" +
                                "return 2+2;" +
                                "}",
                        true
                },

                //II.2-d1-no
                {
                        "main(arg)" +
                                "{" +
                                "return 2+2" +
                                "}",
                        false
                },

                //II.2-d2-no
                {
                        "main(arg)" +
                                "{" +
                                "return;" +
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
