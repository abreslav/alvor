package bool.alvor.programming_language.III.four;

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
 * Time: 18:06
 */

public class TestIIIfour {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRParser interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{

                //III.4-a0-yes
                {
                        "f(first, second, third)" +
                                "{" +
                                "var fourth;" +
                                "if(first==second)" +
                                "{" +
                                "var fifth;" +
                                "fifth=second;" +
                                "fourth=fifth;" +
                                "}" +
                                "else " +
                                "return third;" +
                                "var fifth;" +
                                "fifth=fourth;" +
                                "return fifth;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return f(arg-1, arg, arg+1);" +
                                "}",
                        true
                },

                //III.4-a1-no
                {
                        "f(first, second, third)" +
                                "{" +
                                "var fourth;" +
                                "if(first==second)" +
                                "{" +
                                "var fourth, fifth;" +
                                "fifth=second;" +
                                "fourth=fifth;" +
                                "}" +
                                "else " +
                                "return third;" +
                                "var fifth;" +
                                "fifth=fourth;" +
                                "return fifth;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return f(arg-1, arg, arg+1);" +
                                "}",
                        false
                },

                //III.4-a2-no
                {
                        "f(first, second, third)" +
                                "{" +
                                "var fourth, second;" +
                                "if(first==second)" +
                                "{" +
                                "var fifth;" +
                                "fifth=second;" +
                                "fourth=fifth;" +
                                "}" +
                                "else " +
                                "return third;" +
                                "var fifth;" +
                                "fifth=fourth;" +
                                "return fifth;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return f(arg-1, arg, arg+1);" +
                                "}",
                        false
                },

                //III.4-a3-yes
                {
                        "f(first, second, third)" +
                                "{" +
                                "var fourth;" +
                                "if(first==second)" +
                                "{" +
                                "var fifth;" +
                                "fifth=second;" +
                                "fourth=fifth;" +
                                "}" +
                                "else " +
                                "return third;" +
                                "if(1)" +
                                "var fifth;" +
                                "else " +
                                "while(1)" +
                                "var fifth;" +
                                "var fifth;" +
                                "fifth=fourth;" +
                                "return fifth;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return f(arg-1, arg, arg+1);" +
                                "}",
                        true
                },

                //III.4-a4-no
                {
                        "f(first, second, third)" +
                                "{" +
                                "var fourth;" +
                                "if(first==second)" +
                                "{" +
                                "var fifth;" +
                                "fifth=second;" +
                                "fourth=fifth;" +
                                "}" +
                                "else " +
                                "return third;" +
                                "return fifth;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return f(arg-1, arg, arg+1);" +
                                "}",
                        false
                },

                //III.4-b0-yes
                {
                        "main(arg)" +
                                "{" +
                                "var longidentifier, x;" +
                                "return 1;" +
                                "}",
                        true
                },

                //III.4-b1-no
                {
                        "main(arg)" +
                                "{" +
                                "var longidentifier, arg;" +
                                "return 1;" +
                                "}",
                        false
                },

                //III.4-b2-no
                {
                        "main(arg)" +
                                "{" +
                                "var longidentifier, x, longidentifier;" +
                                "return 1;" +
                                "}",
                        false
                },

                //III.4-c0-yes
                {
                        "main(arg)" +
                                "{" +
                                "if(1)" +
                                "var identifier;" +
                                "var identifier;" +
                                "return 1;" +
                                "}",
                        true
                },

                //III.4-c1-no
                {
                        "main(arg)" +
                                "{" +
                                "if(1)" +
                                "var identifier, identifier;" +
                                "return 1;" +
                                "}",
                        false
                },

                //III.4-c2-no
                {
                        "main(arg)" +
                                "{" +
                                "if(1)" +
                                "var identifier, arg;" +
                                "var identifier;" +
                                "return 1;" +
                                "}",
                        false
                },

                ///III.4-c3-no
                {
                        "main(arg)" +
                                "{" +
                                "var identifier;" +
                                "if(1)" +
                                "var identifier;" +
                                "return 1;" +
                                "}",
                        false
                },

                ///III.4-d0-yes
                {
                        "f(x, y, z)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
                        true
                },

                //III.4-d1-no
                {
                        "f(x, y, x)" +
                                "{" +
                                "return 1;" +
                                "}" +
                                "main(arg)" +
                                "{" +
                                "return 1;" +
                                "}",
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
