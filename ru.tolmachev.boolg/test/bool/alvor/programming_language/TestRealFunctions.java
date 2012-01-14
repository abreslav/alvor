package bool.alvor.programming_language;

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
 * Time: 19:09
 */

public class TestRealFunctions {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRParser interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                //factorial
                {
                        "factorial(n)" +
                                "{" +
                                "if(n>1)" +
                                "return n*factorial(n-1);" +
                                "else " +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return factorial(arg);" +
                                "}",
                        true},

                {
                        "f00bv(a)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "f01ke(i)" +
                                "{" +
                                "return f00bv(i)*f00bv(i);" +
                                "}" +
                                "f02dn(sj)" +
                                "{" +
                                "return f01ke(sj)*f01ke(sj);" +
                                "}" +
                                "f03mc(e)" +
                                "{" +
                                "return f02dn(e)*f01ke(e);" +
                                "}" +
                                "f04rd(s3)" +
                                "{" +
                                "return f03mc(s3)*f02dn(s3);" +
                                "}" +
                                "f05yw(i)" +
                                "{" +
                                "return f04rd(i)*f02dn(i);" +
                                "}" +
                                "f06nn(a)" +
                                "{" +
                                "return f05yw(a)*f03mc(a);" +
                                "}" +
                                "f07qi(uq)" +
                                "{" +
                                "return f06nn(uq)*f03mc(uq);" +
                                "}" +
                                "f08nn(q)" +
                                "{" +
                                "return f07qi(q)*f04rd(q);" +
                                "}" +
                                "f09ki(o8)" +
                                "{" +
                                "return f08nn(o8)*f04rd(o8);" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return f09ki(arg);" +
                                "}",
                        true
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
