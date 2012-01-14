package bool.my.programming_language.I.a;

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
 * Time: 16:13
 */

public class TestI {

    private static final String TABLE_PATH = "trunk\\resources\\programming_language\\table.txt";

    private BooleanLRInterpreter interpreter;

    @DataProvider(name = "inputString")
    public Object[][] createInputStrings() {
        return new Object[][]{
                //I-a1-yes
                {
                        "main(arg){var x,x1az,x1;var qwertyuiop,asdfghjkl" +
                                ",z1x23c445vbn7m890;x=x1az*100/(1+x1)-x%arg;if(x1" +
                                ">x1az&!(x<x1az|x>x1az|x==x1az))while(2+2!=4)x=-x" +
                                ";else x=0;qwertyuiop=asdfghjkl=z1x23c445vbn7m890" +
                                "=x;return x;}",
                        true},

                //I-a2-no
                {
                        "main(arg){varx,x1az,x1;var qwertyuiop,asdfghjkl," +
                                "z1x23c445vbn7m890;x=x1az*100/(1+x1)-x%arg;if(x1>" +
                                "x1az&!(x<x1az|x>x1az|x==x1az))while(2+2!=4)x=-x;" +
                                "else x=0;qwertyuiop=asdfghjkl=z1x23c445vbn7m890=" +
                                "x;return x;}",
                        false},

                //I-b0-yes
                {
                        "main(arg)" +
                                "{" +
                                "var x, elsex;" +
                                "if(arg!=1) 1; else x=1;" +
                                "return 1;" +
                                "}",
                        true},

                //I-b1-no
                {
                        "main(arg)" +
                                "{" +
                                "varx, elsex;" +
                                "if(arg!=1) 1; else x=1;" +
                                "return 1;" +
                                "}",
                        false},

                //I-b2-no
                {
                        "main(arg)" +
                                "{" +
                                "var x, elsex;" +
                                "if(arg!=1) 1; else x=1;" +
                                "return1;" +
                                "}",
                        false
                },

                //I-b3-yes
                {
                        "main(arg)" +
                                "{" +
                                "var x, elsex;" +
                                "if(arg!=1) 1; elsex=1;" +
                                "return 1;" +
                                "}",
                        true
                },

                //I-b4-no
                {
                        "main(arg)" +
                                "{" +
                                "var x, elsex;" +
                                "if(arg! =1) 1; else x=1;" +
                                "return 1;" +
                                "}",
                        false
                },

                //I-b5-no
                {
                        "main(arg)" +
                                "{" +
                                "var x, elsex;" +
                                "if(arg!=1) 1; else x=1;" +
                                "ret urn 1;" +
                                "}",
                        false
                },

                //I-b6-no
                {
                        "main(arg)" +
                                "{" +
                                "var x, while;" +
                                "if(arg!=1) 1; else x=1;" +
                                "return 1;" +
                                "}",
                        false
                },

                //I-b7-no
                {
                        "main(arg)" +
                                "{" +
                                "var x, 7elsex;" +
                                "if(arg!=1) 1; else x=1;" +
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
        interpreter = new BooleanLRInterpreter(table);
    }

    @org.testng.annotations.Test(dataProvider = "inputString")
    public void parse(String input, boolean trueResult) throws Exception {
        interpreter.clearStack();
        boolean result = interpreter.parse(input);
        Assert.assertEquals(result, trueResult);
    }
}
