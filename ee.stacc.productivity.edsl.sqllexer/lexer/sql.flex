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
//DQ_STRING_TEXT=(\\\" | [^\n\r\"] | \\{WHITE_SPACE_CHAR}+\\)*
SQ_STRING_TEXT=(\'\' | [^\n\r\'] | \")*
COMMENT_TEXT=([^*/\n]|[^*\n]"/"[^*\n]|[^/\n]"*"[^/\n]|"*"[^/\n]|"/"[^*\n])*
Ident = {ALPHA}({ALPHA}|{DIGIT}|_)*

%% 

<YYINITIAL> {
  "PARTITION" {/*PARTITION*/}
  "DISTINCT" {/*DISTINCT*/}
  "BETWEEN" {/*BETWEEN*/}
  "VALUES" {/*VALUES*/}
  "SELECT" {/*SELECT*/}
  "DELETE" {/*DELETE*/}
  "INSERT" {/*INSERT*/}
  "ESCAPE" {/*ESCAPE*/}
  "UPDATE" {/*UPDATE*/}
  "EXISTS" {/*EXISTS*/}
  "HAVING" {/*HAVING*/}
  "COMMIT" {/*COMMIT*/}
  "WHERE" {/*WHERE*/}
  "TABLE" {/*TABLE*/}
  "ORDER" {/*ORDER*/}
  "GROUP" {/*GROUP*/}
  "RIGHT" {/*RIGHT*/}
  "INNER" {/*INNER*/}
  "OUTER" {/*OUTER*/}
  "UNION" {/*UNION*/}
  "FROM" {/*FROM*/}
  "OVER" {/*OVER*/}
  "WHEN" {/*WHEN*/}
  "THEN" {/*THEN*/}
  "CASE" {/*CASE*/}
  "CAST" {/*CAST*/}
  "ELSE" {/*ELSE*/}
  "DESC" {/*DESC*/}
  "LIKE" {/*LIKE*/}
  "JOIN" {/*JOIN*/}
  "LEFT" {/*LEFT*/}
  "NULL" {/*NULL*/}
  "FULL" {/*FULL*/}
  "INTO" {/*INTO*/}
  "AND" {/*AND*/}
  "SET" {/*SET*/}
  "END" {/*END*/}
  "ASC" {/*ASC*/}
  "XOR" {/*XOR*/}
  "FOR" {/*FOR*/}
  "NOT" {/*NOT*/}
  "OR" {/*OR*/}
  "ON" {/*ON*/}
  "BY" {/*BY*/}
  "AS" {/*AS*/}
  "IN" {/*IN*/}
  "IS" {/*IS*/}
  "(+)" {/*OUTERJ*/}
  "<>" {/*NE*/}
  "<=" {/*LE*/}
  ">=" {/*GE*/}
  "!=" {/*NE*/}
  "||" {/*CONCAT*/}
  "," {/*,*/}
  "(" {/*(*/}
  ")" {/*)*/}
  "." {/*.*/}
  "+" {/*+*/}
  "-" {/*-*/}
  "*" {/***/}
  "/" {/*/*/}
  "=" {/*=*/}
  "<" {/*<*/}
  ">" {/*>*/}
  "?" {/*?*/}

  {NONNEWLINE_WHITE_SPACE_CHAR}+ {/**/} 
  {DIGIT}+{ALPHA}({DIGIT}|{ALPHA})* {/*DIGAL_ERR*/}
//  \"{DQ_STRING_TEXT}\" {/*STRING_DQ*/}
//  \"{DQ_STRING_TEXT} {/*STRING_DQ_ERR*/} 
  N?\'{SQ_STRING_TEXT}\' {/*STRING_SQ*/}
  \'{SQ_STRING_TEXT} {/*STRING_SQ_ERR*/} 
  {DIGIT}+(\.{DIGIT}+)? { /*NUMBER*/ }  
  {Ident} { /*ID*/ }
  
  . {/*UNKNOWN_CHARACTER_ERR*/}
  
}
