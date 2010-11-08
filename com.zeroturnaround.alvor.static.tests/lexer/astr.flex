package com.zeroturnaround.alvor.string.parser;

import static com.zeroturnaround.alvor.string.parser.TokenType.*;

@SuppressWarnings("unused")

%%

%class AbstractStringLexer
%public
%unicode
%type Token

%eofval{
	return Token.EOF;
%eofval}

ALPHA=[A-Za-z]
DIGIT=[0-9]
NONNEWLINE_WHITE_SPACE_CHAR=[\ \t\b\012]
NEWLINE=[\r\n]
WHITE_SPACE_CHAR=[\n\r\ \t\b\012]
DQ_STRING_TEXT=(\\\" | [^\n\r\"] | \\{WHITE_SPACE_CHAR}+\\)*
SQ_STRING_TEXT=(\\\' | [^\n\r\'] | \\{WHITE_SPACE_CHAR}+\\)*
COMMENT_TEXT=([^*/\n]|[^*\n]"/"[^*\n]|[^/\n]"*"[^/\n]|"*"[^/\n]|"/"[^*\n])*
Ident = {ALPHA}({ALPHA}|{DIGIT}|_)*

%% 


<YYINITIAL> {
  "{" { return Token.OPEN_CURLY; }
  "}" { return Token.CLOSE_CURLY; }
  \[ ([^\n\r\]] | \\\])* \] { return Token.newCharSet(yytext()); }
  "," { return Token.COMMA; }
  "(" {return Token.OPEN_ITER;} 
  ")*" {return Token.CLOSE_ITER;} 
  ")+" {return Token.CLOSE_ITER;} 
  {NEWLINE} {return Token.NEWLINE;}
  {NONNEWLINE_WHITE_SPACE_CHAR} {}
  \"{DQ_STRING_TEXT}\" {return Token.newConstant(yytext());}
}
