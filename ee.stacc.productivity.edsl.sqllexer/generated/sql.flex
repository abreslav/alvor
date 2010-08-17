@SuppressWarnings("unused")

%%

%class SQLLexer
%public
%unicode
%ignorecase
%table

/*%debug*/

ALPHA = [A-Za-z]
DIGIT = [0-9]
NONNEWLINE_WHITE_SPACE_CHAR = [\ \t\b\012]
NEWLINE = \r|\n|\r\n
WHITE_SPACE_CHAR = [\n\r\ \t\b\012]
SQ_STRING_TEXT = (\'\' | [^\n\r\'] | \")*
COMMENT_TEXT = ([^*/\n]|[^*\n]"/"[^*\n]|[^/\n]"*"[^/\n]|"*"[^/\n]|"/"[^*\n])*
Ident = {ALPHA}({ALPHA}|{DIGIT}|_)*

%% 

<YYINITIAL> {
"," {/*,*/}
"(" {/*(*/}
")" {/*)*/}
"{" {/*{*/}
"}" {/*}*/}
"." {/*.*/}
"+" {/*+*/}
"-" {/*-*/}
"*" {/***/}
"/" {/*/*/}
"=" {/*=*/}
"<" {/*<*/}
">" {/*>*/}
"?" {/*?*/}
";" {/*;*/}
":" {/*:*/}
"%" {/*%*/}
{NONNEWLINE_WHITE_SPACE_CHAR}+  {/**/}
\/\*{COMMENT_TEXT}\*\/  {/**/}
--[^\n]*  {/**/}
"(+)" {/*OUTERJ*/}
"<>" | "!=" {/*NE*/}
"<=" {/*LE*/}
">=" {/*GE*/}
"||" {/*CONCAT*/}
":=" {/*COLONEQUALS*/}
"=>" {/*EQUALSGT*/}
{DIGIT}+{ALPHA}({DIGIT}|{ALPHA})* {/*DIGAL_ERR*/}
N?\'{SQ_STRING_TEXT}\' {/*STRING_SQ*/}
N?\'{SQ_STRING_TEXT}  {/*STRING_SQ_ERR*/}
\/\*{COMMENT_TEXT}  {/*MULTILINE_COMMENT_ERR*/}
{DIGIT}+(\.{DIGIT}+)?   {/*NUMBER*/}
{Ident} {/*ID*/}
. {/*UNKNOWN_CHARACTER_ERR*/}
}
