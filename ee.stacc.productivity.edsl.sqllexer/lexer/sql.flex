package ee.stacc.productivity.edsl.sqllexer;

/* this is the scanner example from the JLex website 
   (with small modifications to make it more readable) */

%%

%{
  private int comment_count = 0;
%} 

%class SQLLexer
%char
%state COMMENT
%unicode
%ignorecase

%eofval{
  	return (new Yytoken(-1,yytext(),yyline,yychar,yychar+yylength())); 
%eofval}

/*%debug*/

ALPHA=[A-Za-z]
DIGIT=[0-9]
NONNEWLINE_WHITE_SPACE_CHAR=[\ \t\b\012]
NEWLINE=\r|\n|\r\n
WHITE_SPACE_CHAR=[\n\r\ \t\b\012]
STRING_TEXT=(\\\"|[^\n\r\"]|\\{WHITE_SPACE_CHAR}+\\)*
COMMENT_TEXT=([^*/\n]|[^*\n]"/"[^*\n]|[^/\n]"*"[^/\n]|"*"[^/\n]|"/"[^*\n])*
Ident = {ALPHA}({ALPHA}|{DIGIT}|_)*

%% 

<YYINITIAL> {
  "SELECT" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "FROM" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "AS" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "IN" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "WHERE" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "ORDER" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "BY" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "GROUP" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "JOIN" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "LEFT" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "RIGHT" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "INNER" { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "," { return (new Yytoken(0,yytext(),yyline,yychar,yychar+1)); }
  "(" { return (new Yytoken(3,yytext(),yyline,yychar,yychar+1)); }
  ")" { return (new Yytoken(4,yytext(),yyline,yychar,yychar+1)); }
  "." { return (new Yytoken(9,yytext(),yyline,yychar,yychar+1)); }
  "+" { return (new Yytoken(10,yytext(),yyline,yychar,yychar+1)); }
  "-" { return (new Yytoken(11,yytext(),yyline,yychar,yychar+1)); }
  "*" { return (new Yytoken(12,yytext(),yyline,yychar,yychar+1)); }
  "/" { return (new Yytoken(13,yytext(),yyline,yychar,yychar+1)); }
  "=" { return (new Yytoken(14,yytext(),yyline,yychar,yychar+1)); }


  {DIGIT}+{ALPHA}+ {System.out.println("error: " + yytext()); 
  	return (new Yytoken(-2,"error: " + yytext(),yyline,yychar,yychar+1));
  }

  {NONNEWLINE_WHITE_SPACE_CHAR}+ { }

  \"{STRING_TEXT}\" {
    String str =  yytext().substring(1,yylength()-1);
    return (new Yytoken(40,str,yyline,yychar,yychar+yylength()));
  }
  
  \"{STRING_TEXT} {
    String str =  yytext().substring(1,yytext().length());
    Utility.error(Utility.E_UNCLOSEDSTR);
    return (new Yytoken(-2,"error (unterm str): " + str,yyline,yychar,yychar + str.length()));
  } 
  
  \'{STRING_TEXT}\' {
    String str =  yytext().substring(1,yylength()-1);
    return (new Yytoken(40,str,yyline,yychar,yychar+yylength()));
  }
  
  \'{STRING_TEXT} {
    String str =  yytext().substring(1,yytext().length());
    Utility.error(Utility.E_UNCLOSEDSTR);
    return (new Yytoken(-2,"error (unterm str): " + str,yyline,yychar,yychar + str.length()));
  } 
  
  {DIGIT}+ { return (new Yytoken(42,yytext(),yyline,yychar,yychar+yylength())); }  

  {Ident} { 
  	System.out.println("id: " + yytext());
  	return (new Yytoken(43,yytext(),yyline,yychar,yychar+yylength())); 
  }  
}

. {
  System.out.println("Illegal character: <" + yytext() + ">");
	Utility.error(Utility.E_UNMATCHED);
}
