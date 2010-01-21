import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Generator {

	public static void main(String[] args) throws IOException {
		String inFolder = "generated/";
		String outFolder = "generated/";
		String inClassName = "SQLLexer";
		String outClassName = "SQLLexerGen";
		
		if (args.length > 0) {
			inFolder = args[0];
			inClassName = args[1];
			outFolder = args[2];
			outClassName = args[3];
		}
		File inFile = new File(new File(inFolder), inClassName + ".java");
		File outFile = new File(new File(outFolder),  outClassName + ".java");

		System.out.println("From: " + inFile);
		System.out.println("To: " + outFile);
		
		StringBuilder builder = readFile(inFile);

		Pattern pattern;
		pattern = Pattern.compile("case ([0-9]+):\\s*\\{\\s*/\\*(.*?)\\*/",
				Pattern.MULTILINE				
		);
		Matcher matcher = pattern.matcher(builder);

		StringBuilder tokens = new StringBuilder();
		tokens.append("        System.out.println(\"/** Tokens (action - name)*/\");\n");
		tokens.append("        System.out.println(\"public static final String[] TOKENS = new String[ACTIONS.length];\");\n");
		tokens.append("        System.out.println(\"static {\");\n");
		int maxToken = 0;  
		while (matcher.find()) {
			String index = matcher.group(1);
			String text = matcher.group(2).replace("\n", "\\n");
			tokens.append("        System.out.format(\"    TOKENS[%4d] = \\\"%s\\\";\\n\", " + index + ", \"" + text + "\");\n");
			maxToken = Math.max(maxToken, Integer.parseInt(index));
		}
		tokens.append("        System.out.println(\"}\");\n");
		tokens.append("        System.out.println(\"}\");\n");
		
		
		InputStream main = Generator.class.getResourceAsStream("main.txt");
		StringBuilder mainTemplate = readFile(main);

		
		String replace = 
			builder.toString()
				.replace(inClassName, outClassName)
				;
		FileWriter fileWriter = new FileWriter(outFile);
		fileWriter.write(replace, 0, replace.lastIndexOf('}'));
		fileWriter.write(mainTemplate.toString());
		fileWriter.write(tokens.toString());
		fileWriter.write("\n    }\n}");
		fileWriter.close();
		
		System.out.println(tokens);
	}

	private static StringBuilder readFile(File file)
			throws FileNotFoundException, IOException {
		FileReader reader = new FileReader(file);
		return readFile(reader);
	}

	private static StringBuilder readFile(InputStream in)
	throws FileNotFoundException, IOException {
		Reader reader = new InputStreamReader(in);
		return readFile(reader);
	}
	
	private static StringBuilder readFile(Reader reader) throws IOException {
		int c;
		StringBuilder builder = new StringBuilder();
		while ((c = reader.read()) != -1) {
			builder.append((char) c);
		}
		reader.close();
		return builder;
	}

}
