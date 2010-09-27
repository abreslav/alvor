package com.zeroturnaround.alvor.sqlparser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Command-line interface for specification generator
 * 
 * @author abreslav
 *
 */
public class SpecGeneratorMain {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		final int ARGS_COUNT = 5;

		if (args.length != ARGS_COUNT) {
			System.out.println("Lexer and parser specification generator");
			System.out.println("Command-line arguments:");
			System.out.println("  <syndicated specification> <output gramar> <lexer template> <output lexer> <keywords file>");
			return;
		}
		
		String parserTempName = "grammar/sql.bgtemplate";
		String parserOutName = "grammar/sql.bg";
		String lexerTempName = "grammar/sql.flextemplate";
		String lexerOutName = "grammar/sql.flex";
		String keywordsName = "grammar/sql.keywords";
		
		if (args.length >= ARGS_COUNT) {
			parserTempName = args[0];
			parserOutName = args[1];
			lexerTempName = args[2];
			lexerOutName = args[3];
			keywordsName = args[4];
		}
		
		BufferedReader in = new BufferedReader(new FileReader(parserTempName));
		PrintWriter out = new PrintWriter(parserOutName);
		LexerSpec lexerSpec = SpecGenerator.INSTANCE.processParserSpec(in, out);
		in.close();
		out.close();
		
		in = new BufferedReader(new FileReader(lexerTempName));
		out = new PrintWriter(lexerOutName);
		SpecGenerator.INSTANCE.generateLexerSpec(lexerSpec, in, out);
		out.close();
		in.close();
		
		out = new PrintWriter(keywordsName);
		lexerSpec.printKeywords(out);
		out.close();
	}

}
