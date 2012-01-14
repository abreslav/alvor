package bool.my.programming_language.III.five;

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
 * Time: 18:44
 */

public class TestIIIfive {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{

                //III.5-a0-yes
                {
                        "main(argument)" +
                                "{" +
                                "var anothervariable;" +
                                "return argument+anothervariable;" +
                                "}",
                        true
                },

                //III.5-a1-no
                {
                        "main(argument)" +
                                "{" +
                                "var anothervariable;" +
                                "return argumenth+anothervariable;" +
                                "}",
                        false
                },

                //III.5-a2-no
                {
                        "main(argument)" +
                                "{" +
                                "var anothervariable;" +
                                "return argument+uneautrevariable;" +
                                "}",
                        false
                },

                //III.5-b0-yes
                {
                        "main(argument)" +
                                "{" +
                                "var first;" +
                                "if(1)" +
                                "{" +
                                "var second;" +
                                "second=first;" +
                                "}" +
                                "return first;" +
                                "}",
                        true
                },

                //III.5-b1-no
                {
                        "main(argument)" +
                                "{" +
                                "var first;" +
                                "if(1)" +
                                "{" +
                                "var second;" +
                                "second=first;" +
                                "}" +
                                "return second;" +
                                "}",
                        false
                },

                //III.5-b2-no
                {
                        "main(argument)" +
                                "{" +
                                "var first;" +
                                "if(1)" +
                                "{" +
                                "second=first;" +
                                "var second;" +
                                "}" +
                                "return first;" +
                                "}",
                        false
                },

                //III.5-c0-yes
                {
                        "identifier() { return 1; }" +
                                "main(arg)" +
                                "{" +
                                "var identifier;" +
                                "identifier();" +
                                "return identifier;" +
                                "}",
                        true
                },

                //III.5-c1-yes
                {
                        "identifier() { return 1; }" +
                                "main(arg)" +
                                "{" +
                                "var identifier;" +
                                "identifier();" +
                                "return identifier();" +
                                "}",
                        true
                },

                //III.5-c2-no
                {
                        "identifier() { return 1; }" +
                                "main(arg)" +
                                "{" +
                                "identifier();" +
                                "return identifier;" +
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
