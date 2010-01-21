import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

%%

%class SimpleLexer
%char
%unicode
%table
%int
%{
	public static void main(String[] args) throws IOException {
		SimpleLexer simpleLexer = new SimpleLexer(new StringReader(",,,().."));
		while (true) {
			int yylex = simpleLexer.yylex();
			if (yylex == YYEOF) {
				break;
			}
			System.out.println((char) yylex);
		}
		System.out.println("END");
		
		System.out.println("States: " + ZZ_ROWMAP.length);

		int zzAction;
		
		char[] zzCmap = ZZ_CMAP;
		System.out.println("Character classes:");
		ArrayList<Character> inputs = new ArrayList<Character>();
		for (int i = 0; i < zzCmap.length; i++) {
			char c = zzCmap[i];
			if (c != 0) {
				System.out.print("\"" + ((char) i) + "\" -> " + ((int) c ) + " ");
				inputs.add((char) i);
			}
		}
		System.out.println();

		int[] zzRowmap = ZZ_ROWMAP;
		
		System.out.print("       ");
		for (int zzInput = 0; zzInput < inputs.size(); zzInput++) {
			System.out.format("'%c' ", inputs.get(zzInput));
		}
		System.out.println();
		
		int nonZeroTransitions = 0;
		for (int zzState = 0; zzState < zzRowmap.length; zzState++) {
			System.out.format("%2d -->", zzState);
			for (int i = 0; i < inputs.size(); i++) {
				int zzInput = inputs.get(i); 
				int zzNext = ZZ_TRANS[ZZ_ROWMAP[zzState] + ZZ_CMAP[zzInput]];
				if (zzNext != -1) {
					nonZeroTransitions++;
				}
				System.out.format("%3d ", zzNext);
			}
			int zzAttributes = ZZ_ATTRIBUTE[zzState];
			System.out.format("%3o ", zzAttributes);
			
			System.out.println();
		}
		
		System.out.println("Nonzero transitions: " + nonZeroTransitions);
//		if (zzNext == -1) {
////			break zzForAction;
//		}
//		zzState = zzNext;

//		int zzAttributes = ZZ_ATTRIBUTE[zzState];
//		if ((zzAttributes & 1) == 1) {
//			zzAction = zzState;
////			zzMarkedPosL = zzCurrentPosL;
//			if ((zzAttributes & 8) == 8) {
////				break zzForAction;
//			}
//		}
	}
%}
ALPHA=[A-B]
DIGIT=[0]
NONNEWLINE_WHITE_SPACE_CHAR=[\ \t\b\012]
NEWLINE=\r|\n|\r\n
WHITE_SPACE_CHAR=[\n\r\ \t\b\012]
STRING_TEXT=(\\\"|[^\n\r\"]|\\{WHITE_SPACE_CHAR}+\\)*
COMMENT_TEXT=([^*/\n]|[^*\n]"/"[^*\n]|[^/\n]"*"[^/\n]|"*"[^/\n]|"/"[^*\n])*
Ident = {ALPHA}({ALPHA}|{DIGIT}|_)*

%% 

<YYINITIAL> {
  "," { /* , */ return ',';}
  "(" {  /* ( */ return '(';}
  ")" {  /* ) */ return ')';}
  ".." {  /* . */ return 'X';}
  [0]+ { return '0';}
}

