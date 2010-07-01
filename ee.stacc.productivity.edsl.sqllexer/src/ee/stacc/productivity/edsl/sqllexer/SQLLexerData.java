package ee.stacc.productivity.edsl.sqllexer;

public class SQLLexerData{
/** Char classes (char - class (char)) */
private static final String CHAR_CLASSES_PACKED = 
"\10\0\2\3\1\5\2\0\1\4\22\0\1\3\1\24\1\0\4\0\1\6\1\13\1\14\1\10\1\16" + 
"\1\12\1\17\1\15\1\7\12\2\2\0\1\21\1\20\1\22\1\23\1\0\15\1\1\26\14\1\4\0" + 
"\1\11\1\0\15\1\1\26\14\1\1\0\1\25\uff83\0" + 
"";

public static final char[] CHAR_CLASSES = unpackCharClasses(CHAR_CLASSES_PACKED);

  /** 
   * Copied from JFlex code
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] unpackCharClasses(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    int length = packed.length();
    while (i < length) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

// Alphabet:
// 8 9 10 13 32 !'()*+,-./0123456789<=>?ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz|

/** Number of states (table height) */
public static final int STATE_COUNT = 33;

/** Number of character classes (table width) */
public static final int CHAR_CLASS_COUNT = 23;

/** Transitions (state : state(char class)) */
public static final int[][] TRANSITIONS = {
/*   0 : */{    1,    2,    3,    4,    1,    4,    5,    6,    7,    1,    8,    9,   10,   11,   12,   13,   14,   15,   16,   17,   18,   19,   20, },
/*   1 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   2 : */{   -1,    2,    2,   -1,   -1,   -1,   -1,   -1,   -1,    2,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,    2, },
/*   3 : */{   -1,   21,    3,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   22,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   21, },
/*   4 : */{   -1,   -1,   -1,    4,   -1,    4,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   5 : */{    5,    5,    5,    5,   -1,   -1,   23,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5,    5, },
/*   6 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   7 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   8 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*   9 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   24,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  10 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  11 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  12 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  13 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  14 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  15 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   25,   -1,   26,   -1,   -1,   -1,   -1, },
/*  16 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   27,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  17 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  18 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   26,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  19 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   28,   -1, },
/*  20 : */{   -1,    2,    2,   -1,   -1,   -1,   29,   -1,   -1,    2,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,    2, },
/*  21 : */{   -1,   21,   21,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   21, },
/*  22 : */{   -1,   -1,   30,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  23 : */{   -1,   -1,   -1,   -1,   -1,   -1,    5,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  24 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   31,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  25 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  26 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  27 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  28 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  29 : */{   29,   29,   29,   29,   -1,   -1,   32,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29,   29, },
/*  30 : */{   -1,   -1,   30,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  31 : */{   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
/*  32 : */{   -1,   -1,   -1,   -1,   -1,   -1,   29,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, },
};

/** Attributes (state - attrs (oct)) */
public static final int[] ATTRIBUTES = {
/*   0 */ 0000,
/*   1 */ 0011,
/*   2 */ 0001,
/*   3 */ 0001,
/*   4 */ 0001,
/*   5 */ 0001,
/*   6 */ 0011,
/*   7 */ 0011,
/*   8 */ 0011,
/*   9 */ 0001,
/*  10 */ 0011,
/*  11 */ 0011,
/*  12 */ 0011,
/*  13 */ 0011,
/*  14 */ 0011,
/*  15 */ 0001,
/*  16 */ 0001,
/*  17 */ 0011,
/*  18 */ 0001,
/*  19 */ 0001,
/*  20 */ 0001,
/*  21 */ 0001,
/*  22 */ 0000,
/*  23 */ 0001,
/*  24 */ 0000,
/*  25 */ 0011,
/*  26 */ 0011,
/*  27 */ 0011,
/*  28 */ 0011,
/*  29 */ 0000,
/*  30 */ 0001,
/*  31 */ 0011,
/*  32 */ 0001,
};

/** Actions (state - action) */
public static final int[] ACTIONS = {
/*   0*/    0,
/*   1*/    1,
/*   2*/    2,
/*   3*/    3,
/*   4*/    4,
/*   5*/    5,
/*   6*/    6,
/*   7*/    7,
/*   8*/    8,
/*   9*/    9,
/*  10*/   10,
/*  11*/   11,
/*  12*/   12,
/*  13*/   13,
/*  14*/   14,
/*  15*/   15,
/*  16*/   16,
/*  17*/   17,
/*  18*/    1,
/*  19*/    1,
/*  20*/    2,
/*  21*/   18,
/*  22*/    0,
/*  23*/   19,
/*  24*/    0,
/*  25*/   20,
/*  26*/   21,
/*  27*/   22,
/*  28*/   23,
/*  29*/    0,
/*  30*/    3,
/*  31*/   24,
/*  32*/   19,
};

    public static final String[] KEYWORDS = {
        "PARTITION",
        "DISTINCT",
        "BETWEEN",
        "VALUES",
        "SELECT",
        "DELETE",
        "INSERT",
        "ESCAPE",
        "OVER",
        "UPDATE",
        "EXISTS",
        "HAVING",
        "COMMIT",
        "WHERE",
        "TABLE",
        "ORDER",
        "OR",
        "GROUP",
        "RIGHT",
        "INNER",
        "OUTER",
        "UNION",
        "FROM",
        "WHEN",
        "THEN",
        "CASE",
        "CAST",
        "ELSE",
        "DESC",
        "LIKE",
        "JOIN",
        "LEFT",
        "NULL",
        "FULL",
        "INTO",
        "AND",
        "SET",
        "END",
        "ASC",
        "XOR",
        "FOR",
        "NOT",
        "ON",
        "BY",
        "AS",
        "IN",
        "IS",
        "UNKNOWN",
        "UNKNOWN_BODY",
        "UNKNOWN_LEX",
        "REAL",
        "ACTION",
        "MIN",
        "LOCAL",
        "SECOND",
        "BIT",
        "OCTET_LENGTH",
        "PRECISION",
        "BOTH",
        "SOME",
        "MINUTE",
        "CROSS",
        "DEFERRED",
        "DEFERRABLE",
        "MONTH",
        "SMALLINT",
        "WITH",
        "NCHAR",
        "ZONE",
        "NO",
        "INTERSECT",
        "COUNT",
        "SESSION_USER",
        "NATURAL",
        "DATE",
        "ALL",
        "DOUBLE",
        "NULLIF",
        "CURRENT_DATE",
        "SUM",
        "CORRESPONDING",
        "UNIQUE",
        "ANY",
        "COLLATE",
        "KEY",
        "AVG",
        "INITIALLY",
        "UPPER",
        "TIMESTAMP",
        "CONSTRAINT",
        "LEADING",
        "NUMERIC",
        "DAY",
        "DECIMAL",
        "DEC",
        "EXCEPT",
        "TRUE",
        "MODULE",
        "EXTRACT",
        "CHAR_LENGTH",
        "TIME",
        "SYSTEM_USER",
        "SUBSTRING",
        "INTEGER",
        "CURRENT_TIME",
        "CREATE",
        "PARTIAL",
        "PRIMARY",
        "IMMEDIATE",
        "CHECK",
        "CHARACTER",
        "USER",
        "CHAR",
        "TIMEZONE_HOUR",
        "REFERENCES",
        "MAX",
        "CURRENT_TIMESTAMP",
        "GLOBAL",
        "LOWER",
        "USING",
        "ROWS",
        "TO",
        "CASCADE",
        "TRAILING",
        "TEMPORARY",
        "HOUR",
        "BIT_LENGTH",
        "INDICATOR",
        "FALSE",
        "VALUE",
        "FOREIGN",
        "YEAR",
        "OVERLAPS",
        "CHARACTER_LENGTH",
        "MATCH",
        "INT",
        "CONVERT",
        "NATIONAL",
        "FLOAT",
        "CURRENT_USER",
        "TRANSLATE",
        "INTERVAL",
        "VARCHAR",
        "DEFAULT",
        "VARYING",
        "PRESERVE",
        "POSITION",
        "COALESCE",
        "TRIM",
        "TIMEZONE_MINUTE",
        "AT",
    };
/** Tokens (action - name)*/
public static final String[] TOKENS = new String[ACTIONS.length];
static {
    TOKENS[  10] = ")";
    TOKENS[  14] = "=";
    TOKENS[  12] = "+";
    TOKENS[  17] = "?";
    TOKENS[  13] = "-";
    TOKENS[   4] = "";
    TOKENS[   6] = "/";
    TOKENS[  24] = "OUTERJ";
    TOKENS[   9] = "(";
    TOKENS[  20] = "LE";
    TOKENS[   2] = "ID";
    TOKENS[  15] = "<";
    TOKENS[   1] = "UNKNOWN_CHARACTER_ERR";
    TOKENS[   7] = "*";
    TOKENS[  16] = ">";
    TOKENS[  19] = "STRING_SQ";
    TOKENS[  18] = "DIGAL_ERR";
    TOKENS[   3] = "NUMBER";
    TOKENS[   8] = ",";
    TOKENS[  22] = "GE";
    TOKENS[   5] = "STRING_SQ_ERR";
    TOKENS[  23] = "CONCAT";
    TOKENS[  21] = "NE";
    TOKENS[  11] = ".";
}
}
