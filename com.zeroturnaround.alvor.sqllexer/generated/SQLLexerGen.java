/* The following code was generated by JFlex 1.4.3 on 11/28/10 9:21 PM */

@SuppressWarnings("unused")


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.3
 * on 11/28/10 9:21 PM from the specification file
 * <tt>/home/abreslav/Desktop/ws-loops/com.zeroturnaround.alvor.sqllexer/generated/sql.flex</tt>
 */
public class SQLLexerGen {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\10\0\2\3\1\5\2\0\1\4\22\0\1\3\1\31\1\0\2\0"+
    "\1\30\1\0\1\6\1\13\1\14\1\10\1\20\1\12\1\21\1\17"+
    "\1\7\12\2\1\27\1\26\1\23\1\22\1\24\1\25\1\0\15\1"+
    "\1\33\14\1\4\0\1\11\1\0\15\1\1\33\14\1\1\15\1\32"+
    "\1\16\uff82\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7"+
    "\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17"+
    "\1\20\1\21\1\22\1\23\1\24\1\25\1\26\2\1"+
    "\1\2\1\27\1\0\1\30\1\31\1\0\1\4\1\32"+
    "\1\33\1\34\1\35\1\36\1\37\1\3\2\0\1\40"+
    "\1\31\1\4\2\31\1\4";

  private static int [] zzUnpackAction() {
    int [] result = new int[47];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\34\0\70\0\124\0\160\0\214\0\250\0\34"+
    "\0\34\0\304\0\34\0\34\0\34\0\34\0\34\0\340"+
    "\0\374\0\u0118\0\u0134\0\34\0\34\0\u0150\0\34\0\u016c"+
    "\0\u0188\0\u01a4\0\u01c0\0\u01dc\0\u01f8\0\u0214\0\u0230\0\u024c"+
    "\0\34\0\34\0\34\0\34\0\34\0\34\0\u01dc\0\u0268"+
    "\0\u0284\0\34\0\u02a0\0\34\0\u02bc\0\u02d8\0\u0268";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[47];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int ZZ_TRANS [] = {
    1, 2, 3, 4, 1, 4, 5, 6, 7, 1, 
    8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 
    18, 19, 20, 21, 22, 23, 24, 25, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, 2, 2, -1, 
    -1, -1, -1, -1, -1, 2, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, 2, -1, 26, 3, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, 27, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, 26, -1, -1, -1, 4, -1, 4, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    5, 5, 5, 5, -1, -1, 28, 5, 5, 5, 
    5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 
    5, 5, 5, 5, 5, 5, 5, 5, -1, -1, 
    -1, -1, -1, -1, -1, -1, 29, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, 30, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, 31, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, 32, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, 33, -1, 
    34, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, 35, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, 36, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, 34, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, 37, -1, 
    -1, 2, 2, -1, -1, -1, 5, -1, -1, 2, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, 2, -1, 26, 
    26, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, 26, -1, -1, 38, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    5, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, 29, 29, 29, 29, 29, -1, 29, 39, 
    40, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, 41, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, 31, 31, 
    31, 31, 31, -1, 31, 31, 31, 31, 31, 31, 
    31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 
    31, 31, 31, 31, 31, 31, 29, 29, 29, 29, 
    29, -1, 29, 42, -1, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 29, 29, 29, -1, 
    29, 43, 44, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 29, -1, 29, 42, 
    40, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, -1, 29, 39, 45, 29, 
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, -1, 29, 46, 45, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 
    29, 29, 29, 29, 29, 29, 
  };

  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\5\1\2\11\1\1\5\11\4\1\2\11"+
    "\1\1\1\11\4\1\1\0\2\1\1\0\1\1\6\11"+
    "\1\1\2\0\1\11\1\1\1\11\3\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[47];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public SQLLexerGen(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public SQLLexerGen(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 84) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead > 0) {
      zzEndRead+= numRead;
      return false;
    }
    // unlikely but not impossible: read 0 characters, but not at end of stream    
    if (numRead == 0) {
      int c = zzReader.read();
      if (c == -1) {
        return true;
      } else {
        zzBuffer[zzEndRead++] = (char) c;
        return false;
      }     
    }

	// numRead < 0
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public Yytoken yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 22: 
          { /*%*/
          }
        case 33: break;
        case 26: 
          { /*EQUALSGT*/
          }
        case 34: break;
        case 20: 
          { /*;*/
          }
        case 35: break;
        case 10: 
          { /*)*/
          }
        case 36: break;
        case 16: 
          { /*=*/
          }
        case 37: break;
        case 30: 
          { /*COLONEQUALS*/
          }
        case 38: break;
        case 14: 
          { /*+*/
          }
        case 39: break;
        case 19: 
          { /*?*/
          }
        case 40: break;
        case 15: 
          { /*-*/
          }
        case 41: break;
        case 25: 
          { /*MULTILINE_COMMENT_ERR*/
          }
        case 42: break;
        case 4: 
          { /**/
          }
        case 43: break;
        case 6: 
          { /*/*/
          }
        case 44: break;
        case 11: 
          { /*{*/
          }
        case 45: break;
        case 32: 
          { /*OUTERJ*/
          }
        case 46: break;
        case 21: 
          { /*:*/
          }
        case 47: break;
        case 9: 
          { /*(*/
          }
        case 48: break;
        case 27: 
          { /*LE*/
          }
        case 49: break;
        case 2: 
          { /*ID*/
          }
        case 50: break;
        case 12: 
          { /*}*/
          }
        case 51: break;
        case 17: 
          { /*<*/
          }
        case 52: break;
        case 1: 
          { /*UNKNOWN_CHARACTER_ERR*/
          }
        case 53: break;
        case 7: 
          { /***/
          }
        case 54: break;
        case 18: 
          { /*>*/
          }
        case 55: break;
        case 24: 
          { /*STRING_SQ*/
          }
        case 56: break;
        case 23: 
          { /*DIGAL_ERR*/
          }
        case 57: break;
        case 3: 
          { /*NUMBER*/
          }
        case 58: break;
        case 8: 
          { /*,*/
          }
        case 59: break;
        case 29: 
          { /*GE*/
          }
        case 60: break;
        case 5: 
          { /*STRING_SQ_ERR*/
          }
        case 61: break;
        case 31: 
          { /*CONCAT*/
          }
        case 62: break;
        case 28: 
          { /*NE*/
          }
        case 63: break;
        case 13: 
          { /*.*/
          }
        case 64: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            return null;
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


	public static void main(String[] args) {
		String pack = args[0];
		String className = args[1];
		
		System.out.println("package " + pack + ";");
		System.out.println();
		System.out.println("public class " + className + "{");
		
		System.out.println("/** Char classes (char - class (char)) */");
		System.out.println("private static final String CHAR_CLASSES_PACKED = ");
		{
			int stored = 0;
			int cmpLen = ZZ_CMAP_PACKED.length();
			while (stored < cmpLen) {
				int rightBnd = Math.min(stored + 30, cmpLen);
				System.out.print("\"");
				for (int i = stored; i < rightBnd; i++) {
					char c = ZZ_CMAP_PACKED.charAt(i);
					if (c < 256) {
						System.out.format("\\%o", (int) c);
					} else {
						System.out.format("\\u%4x", (int) c);
					}
				}
				System.out.println("\" + ");
				stored = rightBnd;
			}
			System.out.println("\"\";");
		}
			
		System.out.println();
		System.out.println("public static final char[] CHAR_CLASSES = unpackCharClasses(CHAR_CLASSES_PACKED);");
		System.out.println();
		System.out.println("  /** ");
		System.out.println("   * Copied from JFlex code");
		System.out.println("   * Unpacks the compressed character translation table.");
		System.out.println("   *");
		System.out.println("   * @param packed   the packed character translation table");
		System.out.println("   * @return         the unpacked character translation table");
		System.out.println("   */");
		System.out.println("  private static char [] unpackCharClasses(String packed) {");
		System.out.println("    char [] map = new char[0x10000];");
		System.out.println("    int i = 0;  /* index in packed string  */");
		System.out.println("    int j = 0;  /* index in unpacked array */");
		System.out.println("    int length = packed.length();");
		System.out.println("    while (i < length) {");
		System.out.println("      int  count = packed.charAt(i++);");
		System.out.println("      char value = packed.charAt(i++);");
		System.out.println("      do map[j++] = value; while (--count > 0);");
		System.out.println("    }");
		System.out.println("    return map;");
		System.out.println("  }");
		System.out.println();
		

		
		char[] zzCmap = ZZ_CMAP;
//		System.out.println("public static final char[] CHAR_CLASSES = {");
		int maxCharClass = 0;
		int count = 0;
		int lineCount = 100;
		char[] alphabet = new char[Character.MAX_VALUE + 1];
		int alphCount = 0;
		for (int i = 0; i < zzCmap.length; i++) {
			char c = zzCmap[i];
			if (c > maxCharClass) {
				maxCharClass = c;
			} 
			if (c != 0) {
				alphabet[alphCount] = (char) i;
				alphCount++;
			}
			switch (c) {
				case '\n': 
//					System.out.print("'\\n', ");
					break;
				case '\r': 
//					System.out.print("'\\r', ");
					break;
				case '\"':
//					System.out.print("'\\\"', ");
					break;
				case '\'':
//					System.out.print("'\\\'', ");
					break;
				default:
//					System.out.format("'\\u%04X', ", ((int) c ));
			}
			count++;
			if (count >= lineCount) {
//				System.out.println();
				count = 0;
			}
		}
//		System.out.println("};");

		System.out.println("// Alphabet:");
		System.out.print("// ");
		for (int i = 0; i < alphCount; i++) {
			if (alphabet[i] <= 32) {
				System.out.print(((int) alphabet[i]) + " ");
			} else {
				System.out.print(alphabet[i]);
			}
		}
		System.out.println("\n");
		
		int[] zzRowmap = ZZ_ROWMAP;
		System.out.println("/** Number of states (table height) */");
		System.out.println("public static final int STATE_COUNT = " + zzRowmap.length + ";");
		System.out.println();

		System.out.println("/** Number of character classes (table width) */");
		System.out.println("public static final int CHAR_CLASS_COUNT = " + (maxCharClass + 1) + ";");
		System.out.println();
		
		System.out.println("/** Transitions (state : state(char class)) */");
		System.out.println("public static final int[][] TRANSITIONS = {");
		for (int zzState = 0; zzState < zzRowmap.length; zzState++) {
			System.out.format("/*%4d : */", zzState);
			System.out.print("{ ");
			for (int charClass = 0; charClass <= maxCharClass; charClass++) {
				int zzNext = ZZ_TRANS[ZZ_ROWMAP[zzState] + charClass];
				System.out.format("%4d, ", zzNext);
			}
			System.out.println("},");
		}
		System.out.println("};");
		System.out.println();
		
		System.out.println("/** Attributes (state - attrs (oct)) */");
		System.out.println("public static final int[] ATTRIBUTES = {");
		for (int zzState = 0; zzState < zzRowmap.length; zzState++) {
			int zzAttributes = ZZ_ATTRIBUTE[zzState];
			System.out.format("/*%4d */ %04o,\n", zzState, zzAttributes);
		}
		System.out.println("};");
		System.out.println();
		
		System.out.println("/** Actions (state - action) */");
		System.out.println("public static final int[] ACTIONS = {");
		int[] zzAction = ZZ_ACTION;
		for (int i = 0; i < zzAction.length; i++) {
			System.out.format("/*%4d*/ %4d,\n", i, zzAction[i]);
		}
		System.out.println("};");
		System.out.println();
System.out.println("    public static final String[] KEYWORDS = {");
System.out.println("        \"PARTITION\",");
System.out.println("        \"IMMEDIATE\",");
System.out.println("        \"DISTINCT\",");
System.out.println("        \"EXECUTE\",");
System.out.println("        \"BETWEEN\",");
System.out.println("        \"DECLARE\",");
System.out.println("        \"VALUES\",");
System.out.println("        \"SELECT\",");
System.out.println("        \"DELETE\",");
System.out.println("        \"INSERT\",");
System.out.println("        \"ESCAPE\",");
System.out.println("        \"OVER\",");
System.out.println("        \"UPDATE\",");
System.out.println("        \"EXISTS\",");
System.out.println("        \"HAVING\",");
System.out.println("        \"COMMIT\",");
System.out.println("        \"WHERE\",");
System.out.println("        \"BEGIN\",");
System.out.println("        \"TABLE\",");
System.out.println("        \"ORDER\",");
System.out.println("        \"OR\",");
System.out.println("        \"GROUP\",");
System.out.println("        \"RIGHT\",");
System.out.println("        \"INNER\",");
System.out.println("        \"OUTER\",");
System.out.println("        \"UNION\",");
System.out.println("        \"FROM\",");
System.out.println("        \"WHEN\",");
System.out.println("        \"THEN\",");
System.out.println("        \"CASE\",");
System.out.println("        \"CAST\",");
System.out.println("        \"CALL\",");
System.out.println("        \"ELSE\",");
System.out.println("        \"DESC\",");
System.out.println("        \"LIKE\",");
System.out.println("        \"JOIN\",");
System.out.println("        \"LEFT\",");
System.out.println("        \"NULL\",");
System.out.println("        \"FULL\",");
System.out.println("        \"INTO\",");
System.out.println("        \"AND\",");
System.out.println("        \"SET\",");
System.out.println("        \"END\",");
System.out.println("        \"ASC\",");
System.out.println("        \"XOR\",");
System.out.println("        \"FOR\",");
System.out.println("        \"NOT\",");
System.out.println("        \"ON\",");
System.out.println("        \"BY\",");
System.out.println("        \"AS\",");
System.out.println("        \"IN\",");
System.out.println("        \"IS\",");
System.out.println("        \"UNKNOWN\",");
System.out.println("        \"UNKNOWN_BODY\",");
System.out.println("        \"UNKNOWN_LEX\",");
System.out.println("        \"REAL\",");
System.out.println("        \"ACTION\",");
System.out.println("        \"MIN\",");
System.out.println("        \"LOCAL\",");
System.out.println("        \"SECOND\",");
System.out.println("        \"BIT\",");
System.out.println("        \"OCTET_LENGTH\",");
System.out.println("        \"PRECISION\",");
System.out.println("        \"BOTH\",");
System.out.println("        \"SOME\",");
System.out.println("        \"MINUTE\",");
System.out.println("        \"CROSS\",");
System.out.println("        \"DEFERRED\",");
System.out.println("        \"DEFERRABLE\",");
System.out.println("        \"MONTH\",");
System.out.println("        \"SMALLINT\",");
System.out.println("        \"WITH\",");
System.out.println("        \"NCHAR\",");
System.out.println("        \"ZONE\",");
System.out.println("        \"NO\",");
System.out.println("        \"INTERSECT\",");
System.out.println("        \"COUNT\",");
System.out.println("        \"SESSION_USER\",");
System.out.println("        \"NATURAL\",");
System.out.println("        \"DATE\",");
System.out.println("        \"ALL\",");
System.out.println("        \"DOUBLE\",");
System.out.println("        \"NULLIF\",");
System.out.println("        \"CURRENT_DATE\",");
System.out.println("        \"SUM\",");
System.out.println("        \"CORRESPONDING\",");
System.out.println("        \"UNIQUE\",");
System.out.println("        \"ANY\",");
System.out.println("        \"COLLATE\",");
System.out.println("        \"KEY\",");
System.out.println("        \"AVG\",");
System.out.println("        \"INITIALLY\",");
System.out.println("        \"UPPER\",");
System.out.println("        \"TIMESTAMP\",");
System.out.println("        \"CONSTRAINT\",");
System.out.println("        \"LEADING\",");
System.out.println("        \"NUMERIC\",");
System.out.println("        \"DAY\",");
System.out.println("        \"DECIMAL\",");
System.out.println("        \"DEC\",");
System.out.println("        \"EXCEPT\",");
System.out.println("        \"TRUE\",");
System.out.println("        \"MODULE\",");
System.out.println("        \"EXTRACT\",");
System.out.println("        \"CHAR_LENGTH\",");
System.out.println("        \"TIME\",");
System.out.println("        \"SYSTEM_USER\",");
System.out.println("        \"SUBSTRING\",");
System.out.println("        \"INTEGER\",");
System.out.println("        \"CURRENT_TIME\",");
System.out.println("        \"CREATE\",");
System.out.println("        \"PARTIAL\",");
System.out.println("        \"PRIMARY\",");
System.out.println("        \"CHECK\",");
System.out.println("        \"CHARACTER\",");
System.out.println("        \"USER\",");
System.out.println("        \"CHAR\",");
System.out.println("        \"TIMEZONE_HOUR\",");
System.out.println("        \"REFERENCES\",");
System.out.println("        \"MAX\",");
System.out.println("        \"CURRENT_TIMESTAMP\",");
System.out.println("        \"GLOBAL\",");
System.out.println("        \"LOWER\",");
System.out.println("        \"USING\",");
System.out.println("        \"ROWS\",");
System.out.println("        \"TO\",");
System.out.println("        \"CASCADE\",");
System.out.println("        \"TRAILING\",");
System.out.println("        \"TEMPORARY\",");
System.out.println("        \"HOUR\",");
System.out.println("        \"BIT_LENGTH\",");
System.out.println("        \"INDICATOR\",");
System.out.println("        \"FALSE\",");
System.out.println("        \"VALUE\",");
System.out.println("        \"FOREIGN\",");
System.out.println("        \"YEAR\",");
System.out.println("        \"OVERLAPS\",");
System.out.println("        \"CHARACTER_LENGTH\",");
System.out.println("        \"MATCH\",");
System.out.println("        \"INT\",");
System.out.println("        \"CONVERT\",");
System.out.println("        \"NATIONAL\",");
System.out.println("        \"FLOAT\",");
System.out.println("        \"CURRENT_USER\",");
System.out.println("        \"TRANSLATE\",");
System.out.println("        \"INTERVAL\",");
System.out.println("        \"VARCHAR\",");
System.out.println("        \"DEFAULT\",");
System.out.println("        \"VARYING\",");
System.out.println("        \"PRESERVE\",");
System.out.println("        \"POSITION\",");
System.out.println("        \"COALESCE\",");
System.out.println("        \"TRIM\",");
System.out.println("        \"TIMEZONE_MINUTE\",");
System.out.println("        \"AT\",");
System.out.println("    };");
        System.out.println("/** Tokens (action - name)*/");
        System.out.println("public static final String[] TOKENS = new String[ACTIONS.length];");
        System.out.println("static {");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 22, "%");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 26, "EQUALSGT");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 20, ";");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 10, ")");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 16, "=");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 30, "COLONEQUALS");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 14, "+");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 19, "?");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 15, "-");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 25, "MULTILINE_COMMENT_ERR");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 4, "");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 6, "/");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 11, "{");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 32, "OUTERJ");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 21, ":");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 9, "(");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 27, "LE");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 2, "ID");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 12, "}");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 17, "<");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 1, "UNKNOWN_CHARACTER_ERR");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 7, "*");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 18, ">");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 24, "STRING_SQ");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 23, "DIGAL_ERR");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 3, "NUMBER");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 8, ",");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 29, "GE");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 5, "STRING_SQ_ERR");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 31, "CONCAT");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 28, "NE");
        System.out.format("    TOKENS[%4d] = \"%s\";\n", 13, ".");
        System.out.println("}");
        System.out.println("}");

    }
}