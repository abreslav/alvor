%%

%class SQLLexer
%public
%unicode
%ignorecase
%table

/*%debug*/

ALPHA=[A-Za-z]
DIGIT=[0-9]
NONNEWLINE_WHITE_SPACE_CHAR=[\ \t\b\012]
NEWLINE=\r|\n|\r\n
WHITE_SPACE_CHAR=[\n\r\ \t\b\012]
DQ_STRING_TEXT=(\\\" | [^\n\r\"] | \\{WHITE_SPACE_CHAR}+\\)*
SQ_STRING_TEXT=(\\\' | [^\n\r\'] | \\{WHITE_SPACE_CHAR}+\\)*
COMMENT_TEXT=([^*/\n]|[^*\n]"/"[^*\n]|[^/\n]"*"[^/\n]|"*"[^/\n]|"/"[^*\n])*
Ident = {ALPHA}({ALPHA}|{DIGIT}|_)*

%% 

<YYINITIAL> {
  "SELECT" { /*SELECT*/ }
  "FROM" { /*FROM*/ }
  "AS" { /*AS*/ }
  "IN" { /*IN*/ }
  "WHERE" { /*WHERE*/ }
  "ORDER" {/*ORDER*/}
  "BY" {/*BY*/}
  "GROUP" {/*GROUP*/}
  "JOIN" {/*JOIN*/}
  "LEFT" {/*LEFT*/}
  "RIGHT" {/*RIGHT*/}
  "INNER" {/*INNER*/}
  "," {/*,*/}
  "(" {/*(*/}
  ")" {/*)*/}
  "." {/*.*/}
  "+" {/*+*/}
  "-" {/*-*/}
  "*" {/***/}
  "/" {/*/*/}
  "=" { /*=*/ }

  {NONNEWLINE_WHITE_SPACE_CHAR}+ {/*WS*/}

  {DIGIT}+{ALPHA}+ {/*ERROR_DIGAL*/}

  \"{DQ_STRING_TEXT}\" {/*STRING_DQ*/}
  
  \"{DQ_STRING_TEXT} {/*STRING_DQ_ERR*/} 
  
  \'{SQ_STRING_TEXT}\' {/*STRING_SQ*/}
  
  \'{SQ_STRING_TEXT} {/*STRING_SQ_ERR*/} 
  
  {DIGIT}+ { /*NUMBER*/ }  

  {Ident} { /*ID*/ }
  
}
