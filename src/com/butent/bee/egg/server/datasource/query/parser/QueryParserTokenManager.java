package com.butent.bee.egg.server.datasource.query.parser;

import java.io.PrintStream;

public class QueryParserTokenManager implements QueryParserConstants {

  public static final String[] jjstrLiteralImages = {
      "", null, null, null, null, null, null, null, null, null, null, null, null,
      null, null, null, null, null, null, null, null, null, null, null, null, null, null,
      null, null, null, null, null, null, null, null, null, null, null, null, null, null,
      null, null, null, null, null, null, null, null, null, null, null, null, null, null,
      null, null, null, null, null, null, null, null, null, null, "\54", "\50", "\51",
      "\75", null, "\74", "\74\75", "\76", "\76\75", "\52", "\53", "\55", "\57", "\45",
      null,};

  public static final String[] lexStateNames = {
      "DEFAULT",
  };

  static final long[] jjbitVec0 = {
      0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
  };

  static final long[] jjbitVec2 = {
      0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
  };

  static final int[] jjnextStates = {
      18, 19, 20, 11, 12, 5, 6, 8, 9,
  };

  static final long[] jjtoToken = {
      0x8fffffffffffffe1L, 0xffffL,
  };

  static final long[] jjtoSkip = {
      0x1eL, 0x0L,
  };

  private static boolean jjCanMove0(int hiByte, int i1, int i2, long l1, long l2) {
    switch (hiByte) {
      case 0:
        return ((jjbitVec2[i2] & l2) != 0L);
      default:
        if ((jjbitVec0[i1] & l1) != 0L) {
          return true;
        }
        return false;
    }
  }

  public PrintStream debugStream = System.out;

  protected JavaCharStream inputStream;

  protected char curChar;

  int curLexState = 0;
  int defaultLexState = 0;

  int jjnewStateCnt;

  int jjround;

  int jjmatchedPos;

  int jjmatchedKind;

  private final int[] jjrounds = new int[22];
  private final int[] jjstateSet = new int[44];

  public QueryParserTokenManager(JavaCharStream stream) {
    if (JavaCharStream.staticFlag) {
      throw new Error(
          "ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
    }
    inputStream = stream;
  }

  public QueryParserTokenManager(JavaCharStream stream, int lexState) {
    this(stream);
    switchTo(lexState);
  }

  public Token getNextToken() {
    Token matchedToken;
    int curPos = 0;

    EOFLoop : for (;;) {
      try {
        curChar = inputStream.beginToken();
      } catch (java.io.IOException e) {
        jjmatchedKind = 0;
        matchedToken = jjFillToken();
        return matchedToken;
      }

      jjmatchedKind = 0x7fffffff;
      jjmatchedPos = 0;
      curPos = jjMoveStringLiteralDfa00();
      if (jjmatchedPos == 0 && jjmatchedKind > 79) {
        jjmatchedKind = 79;
      }
      if (jjmatchedKind != 0x7fffffff) {
        if (jjmatchedPos + 1 < curPos) {
          inputStream.backup(curPos - jjmatchedPos - 1);
        }
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
          matchedToken = jjFillToken();
          return matchedToken;
        } else {
          continue EOFLoop;
        }
      }
      int errorLine = inputStream.getEndLine();
      int errorColumn = inputStream.getEndColumn();
      String errorAfter = null;
      boolean eofSeen = false;
      try {
        inputStream.readChar();
        inputStream.backup(1);
      } catch (java.io.IOException e1) {
        eofSeen = true;
        errorAfter = curPos <= 1 ? "" : inputStream.getImage();
        if (curChar == '\n' || curChar == '\r') {
          errorLine++;
          errorColumn = 0;
        } else {
          errorColumn++;
        }
      }
      if (!eofSeen) {
        inputStream.backup(1);
        errorAfter = curPos <= 1 ? "" : inputStream.getImage();
      }
      throw new TokenMgrError(eofSeen, curLexState, errorLine, errorColumn, errorAfter, curChar,
          TokenMgrError.LEXICAL_ERROR);
    }
  }

  public void reInit(JavaCharStream stream) {
    jjmatchedPos = jjnewStateCnt = 0;
    curLexState = defaultLexState;
    inputStream = stream;
    reInitRounds();
  }

  public void reInit(JavaCharStream stream, int lexState) {
    reInit(stream);
    switchTo(lexState);
  }

  public void setDebugStream(java.io.PrintStream ds) {
    debugStream = ds;
  }

  public void switchTo(int lexState) {
    if (lexState >= 1 || lexState < 0) {
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState
          + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
    } else {
      curLexState = lexState;
    }
  }

  protected Token jjFillToken() {
    final Token t;
    final String curTokenImage;
    final int beginLine;
    final int endLine;
    final int beginColumn;
    final int endColumn;
    String im = jjstrLiteralImages[jjmatchedKind];
    curTokenImage = (im == null) ? inputStream.getImage() : im;
    beginLine = inputStream.getBeginLine();
    beginColumn = inputStream.getBeginColumn();
    endLine = inputStream.getEndLine();
    endColumn = inputStream.getEndColumn();
    t = Token.newToken(jjmatchedKind, curTokenImage);

    t.beginLine = beginLine;
    t.endLine = endLine;
    t.beginColumn = beginColumn;
    t.endColumn = endColumn;

    return t;
  }

  private void jjAddStates(int start, int end) {
    do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
    } while (start++ != end);
  }

  private void jjCheckNAdd(int state) {
    if (jjrounds[state] != jjround) {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
    }
  }

  private void jjCheckNAddStates(int start, int end) {
    do {
      jjCheckNAdd(jjnextStates[start]);
    } while (start++ != end);
  }

  private void jjCheckNAddTwoStates(int state1, int state2) {
    jjCheckNAdd(state1);
    jjCheckNAdd(state2);
  }

  private int jjMoveNfa0(int startState, int curPos) {
    int strKind = jjmatchedKind;
    int strPos = jjmatchedPos;
    int seenUpto;
    inputStream.backup(seenUpto = curPos + 1);
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      throw new Error("Internal Error");
    }
    curPos = 0;
    int startsAt = 0;
    jjnewStateCnt = 22;
    int i = 1;
    jjstateSet[0] = startState;
    int kind = 0x7fffffff;
    for (;;) {
      if (++jjround == 0x7fffffff) {
        reInitRounds();
      }
      if (curChar < 64) {
        long l = 1L << curChar;
        do {
          switch (jjstateSet[--i]) {
            case 0:
              if ((0x3ff000000000000L & l) != 0L) {
                if (kind > 58) {
                  kind = 58;
                }
                jjCheckNAddStates(0, 2);
              } else if (curChar == 60) {
                jjstateSet[jjnewStateCnt++] = 15;
              } else if (curChar == 33) {
                jjstateSet[jjnewStateCnt++] = 13;
              } else if (curChar == 34) {
                jjCheckNAddTwoStates(8, 9);
              } else if (curChar == 39) {
                jjCheckNAddTwoStates(5, 6);
              } else if (curChar == 46) {
                jjCheckNAdd(3);
              }
              break;
            case 1:
              if ((0x3ff000000000000L & l) == 0L) {
                break;
              }
              if (kind > 57) {
                kind = 57;
              }
              jjstateSet[jjnewStateCnt++] = 1;
              break;
            case 2:
              if (curChar == 46) {
                jjCheckNAdd(3);
              }
              break;
            case 3:
              if ((0x3ff000000000000L & l) == 0L) {
                break;
              }
              if (kind > 59) {
                kind = 59;
              }
              jjCheckNAdd(3);
              break;
            case 4:
              if (curChar == 39) {
                jjCheckNAddTwoStates(5, 6);
              }
              break;
            case 5:
              if ((0xffffff7fffffffffL & l) != 0L) {
                jjCheckNAddTwoStates(5, 6);
              }
              break;
            case 6:
              if (curChar == 39 && kind > 63) {
                kind = 63;
              }
              break;
            case 7:
              if (curChar == 34) {
                jjCheckNAddTwoStates(8, 9);
              }
              break;
            case 8:
              if ((0xfffffffbffffffffL & l) != 0L) {
                jjCheckNAddTwoStates(8, 9);
              }
              break;
            case 9:
              if (curChar == 34 && kind > 63) {
                kind = 63;
              }
              break;
            case 11:
              jjAddStates(3, 4);
              break;
            case 13:
              if (curChar == 61 && kind > 69) {
                kind = 69;
              }
              break;
            case 14:
              if (curChar == 33) {
                jjstateSet[jjnewStateCnt++] = 13;
              }
              break;
            case 15:
              if (curChar == 62 && kind > 69) {
                kind = 69;
              }
              break;
            case 16:
              if (curChar == 60) {
                jjstateSet[jjnewStateCnt++] = 15;
              }
              break;
            case 17:
              if ((0x3ff000000000000L & l) == 0L) {
                break;
              }
              if (kind > 58) {
                kind = 58;
              }
              jjCheckNAddStates(0, 2);
              break;
            case 18:
              if ((0x3ff000000000000L & l) == 0L) {
                break;
              }
              if (kind > 58) {
                kind = 58;
              }
              jjCheckNAdd(18);
              break;
            case 19:
              if ((0x3ff000000000000L & l) != 0L) {
                jjCheckNAddTwoStates(19, 20);
              }
              break;
            case 20:
              if (curChar != 46) {
                break;
              }
              if (kind > 59) {
                kind = 59;
              }
              jjCheckNAdd(21);
              break;
            case 21:
              if ((0x3ff000000000000L & l) == 0L) {
                break;
              }
              if (kind > 59) {
                kind = 59;
              }
              jjCheckNAdd(21);
              break;
            default:
              break;
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        long l = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            case 0:
              if ((0x7fffffe87fffffeL & l) != 0L) {
                if (kind > 57) {
                  kind = 57;
                }
                jjCheckNAdd(1);
              } else if (curChar == 96) {
                jjCheckNAdd(11);
              }
              break;
            case 1:
              if ((0x7fffffe87fffffeL & l) == 0L) {
                break;
              }
              if (kind > 57) {
                kind = 57;
              }
              jjCheckNAdd(1);
              break;
            case 5:
              jjAddStates(5, 6);
              break;
            case 8:
              jjAddStates(7, 8);
              break;
            case 10:
              if (curChar == 96) {
                jjCheckNAdd(11);
              }
              break;
            case 11:
              if ((0xfffffffeffffffffL & l) != 0L) {
                jjCheckNAddTwoStates(11, 12);
              }
              break;
            case 12:
              if (curChar == 96 && kind > 64) {
                kind = 64;
              }
              break;
            default:
              break;
          }
        } while (i != startsAt);
      } else {
        int hiByte = (curChar >> 8);
        int i1 = hiByte >> 6;
        long l1 = 1L << (hiByte & 077);
        int i2 = (curChar & 0xff) >> 6;
        long l2 = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            case 5:
              if (jjCanMove0(hiByte, i1, i2, l1, l2)) {
                jjAddStates(5, 6);
              }
              break;
            case 8:
              if (jjCanMove0(hiByte, i1, i2, l1, l2)) {
                jjAddStates(7, 8);
              }
              break;
            case 11:
              if (jjCanMove0(hiByte, i1, i2, l1, l2)) {
                jjAddStates(3, 4);
              }
              break;
            default:
              break;
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        jjmatchedKind = kind;
        jjmatchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 22 - (jjnewStateCnt = startsAt))) {
        break;
      }
      try {
        curChar = inputStream.readChar();
      } catch (java.io.IOException e) {
        break;
      }
    }
    if (jjmatchedPos > strPos) {
      return curPos;
    }

    int toRet = Math.max(curPos, seenUpto);

    if (curPos < toRet) {
      for (i = toRet - Math.min(curPos, seenUpto); i-- > 0;) {
        try {
          curChar = inputStream.readChar();
        } catch (java.io.IOException e) {
          throw new Error("Internal Error : Please send a bug report.");
        }
      }
    }

    if (jjmatchedPos < strPos) {
      jjmatchedKind = strKind;
      jjmatchedPos = strPos;
    } else if (jjmatchedPos == strPos && jjmatchedKind > strKind) {
      jjmatchedKind = strKind;
    }

    return toRet;
  }

  private int jjMoveStringLiteralDfa00() {
    switch (curChar) {
      case 9:
        jjmatchedKind = 2;
        return jjMoveNfa0(0, 0);
      case 10:
        jjmatchedKind = 3;
        return jjMoveNfa0(0, 0);
      case 13:
        jjmatchedKind = 4;
        return jjMoveNfa0(0, 0);
      case 32:
        jjmatchedKind = 1;
        return jjMoveNfa0(0, 0);
      case 37:
        jjmatchedKind = 78;
        return jjMoveNfa0(0, 0);
      case 40:
        jjmatchedKind = 66;
        return jjMoveNfa0(0, 0);
      case 41:
        jjmatchedKind = 67;
        return jjMoveNfa0(0, 0);
      case 42:
        jjmatchedKind = 74;
        return jjMoveNfa0(0, 0);
      case 43:
        jjmatchedKind = 75;
        return jjMoveNfa0(0, 0);
      case 44:
        jjmatchedKind = 65;
        return jjMoveNfa0(0, 0);
      case 45:
        jjmatchedKind = 76;
        return jjMoveNfa0(0, 0);
      case 47:
        jjmatchedKind = 77;
        return jjMoveNfa0(0, 0);
      case 60:
        jjmatchedKind = 70;
        return jjMoveStringLiteralDfa10(0x0L, 0x80L);
      case 61:
        jjmatchedKind = 68;
        return jjMoveNfa0(0, 0);
      case 62:
        jjmatchedKind = 72;
        return jjMoveStringLiteralDfa10(0x0L, 0x200L);
      case 65:
        return jjMoveStringLiteralDfa10(0x40220000L, 0x0L);
      case 66:
        return jjMoveStringLiteralDfa10(0x400L, 0x0L);
      case 67:
        return jjMoveStringLiteralDfa10(0x200080000000L, 0x0L);
      case 68:
        return jjMoveStringLiteralDfa10(0x88008005040000L, 0x0L);
      case 69:
        return jjMoveStringLiteralDfa10(0x800000000000L, 0x0L);
      case 70:
        return jjMoveStringLiteralDfa10(0x108000L, 0x0L);
      case 71:
        return jjMoveStringLiteralDfa10(0x80L, 0x0L);
      case 72:
        return jjMoveStringLiteralDfa10(0x10000000000L, 0x0L);
      case 73:
        return jjMoveStringLiteralDfa10(0x800000000L, 0x0L);
      case 76:
        return jjMoveStringLiteralDfa10(0x22000000005000L, 0x0L);
      case 77:
        return jjMoveStringLiteralDfa10(0x10a4030000000L, 0x0L);
      case 78:
        return jjMoveStringLiteralDfa10(0x4001600800000L, 0x0L);
      case 79:
        return jjMoveStringLiteralDfa10(0x412200L, 0x0L);
      case 80:
        return jjMoveStringLiteralDfa10(0x100L, 0x0L);
      case 81:
        return jjMoveStringLiteralDfa10(0x10000000000000L, 0x0L);
      case 83:
        return jjMoveStringLiteralDfa10(0x440100000820L, 0x0L);
      case 84:
        return jjMoveStringLiteralDfa10(0x10000000a080000L, 0x0L);
      case 85:
        return jjMoveStringLiteralDfa10(0x40000000000000L, 0x0L);
      case 87:
        return jjMoveStringLiteralDfa10(0x100000000040L, 0x0L);
      case 89:
        return jjMoveStringLiteralDfa10(0x2000000000L, 0x0L);
      case 97:
        return jjMoveStringLiteralDfa10(0x40220000L, 0x0L);
      case 98:
        return jjMoveStringLiteralDfa10(0x400L, 0x0L);
      case 99:
        return jjMoveStringLiteralDfa10(0x200080000000L, 0x0L);
      case 100:
        return jjMoveStringLiteralDfa10(0x88008005040000L, 0x0L);
      case 101:
        return jjMoveStringLiteralDfa10(0x800000000000L, 0x0L);
      case 102:
        return jjMoveStringLiteralDfa10(0x108000L, 0x0L);
      case 103:
        return jjMoveStringLiteralDfa10(0x80L, 0x0L);
      case 104:
        return jjMoveStringLiteralDfa10(0x10000000000L, 0x0L);
      case 105:
        return jjMoveStringLiteralDfa10(0x800000000L, 0x0L);
      case 108:
        return jjMoveStringLiteralDfa10(0x22000000005000L, 0x0L);
      case 109:
        return jjMoveStringLiteralDfa10(0x10a4030000000L, 0x0L);
      case 110:
        return jjMoveStringLiteralDfa10(0x4001600800000L, 0x0L);
      case 111:
        return jjMoveStringLiteralDfa10(0x412200L, 0x0L);
      case 112:
        return jjMoveStringLiteralDfa10(0x100L, 0x0L);
      case 113:
        return jjMoveStringLiteralDfa10(0x10000000000000L, 0x0L);
      case 115:
        return jjMoveStringLiteralDfa10(0x440100000820L, 0x0L);
      case 116:
        return jjMoveStringLiteralDfa10(0x10000000a080000L, 0x0L);
      case 117:
        return jjMoveStringLiteralDfa10(0x40000000000000L, 0x0L);
      case 119:
        return jjMoveStringLiteralDfa10(0x100000000040L, 0x0L);
      case 121:
        return jjMoveStringLiteralDfa10(0x2000000000L, 0x0L);
      default:
        return jjMoveNfa0(0, 0);
    }
  }

  private int jjMoveStringLiteralDfa10(long active0, long active1) {
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 0);
    }
    switch (curChar) {
      case 61:
        if ((active1 & 0x80L) != 0L) {
          jjmatchedKind = 71;
          jjmatchedPos = 1;
        } else if ((active1 & 0x200L) != 0L) {
          jjmatchedKind = 73;
          jjmatchedPos = 1;
        }
        break;
      case 65:
        return jjMoveStringLiteralDfa20(active0, 0x89008025104000L, active1, 0L);
      case 69:
        return jjMoveStringLiteralDfa20(active0, 0x42000040020L, active1, 0L);
      case 70:
        return jjMoveStringLiteralDfa20(active0, 0x2000L, active1, 0L);
      case 72:
        return jjMoveStringLiteralDfa20(active0, 0x40L, active1, 0L);
      case 73:
        return jjMoveStringLiteralDfa20(active0, 0x21a001a001100L, active1, 0L);
      case 75:
        return jjMoveStringLiteralDfa20(active0, 0x800L, active1, 0L);
      case 78:
        return jjMoveStringLiteralDfa20(active0, 0x800000200000L, active1, 0L);
      case 79:
        return jjMoveStringLiteralDfa20(active0, 0x124214680808000L, active1, 0L);
      case 80:
        return jjMoveStringLiteralDfa20(active0, 0x40000000010000L, active1, 0L);
      case 82:
        if ((active0 & 0x400000L) != 0L) {
          jjmatchedKind = 22;
          jjmatchedPos = 1;
        }
        return jjMoveStringLiteralDfa20(active0, 0x80280L, active1, 0L);
      case 83:
        if ((active0 & 0x800000000L) != 0L) {
          jjmatchedKind = 35;
          jjmatchedPos = 1;
        }
        return jjMoveStringLiteralDfa20(active0, 0x20000L, active1, 0L);
      case 84:
        return jjMoveStringLiteralDfa20(active0, 0x400000000000L, active1, 0L);
      case 85:
        return jjMoveStringLiteralDfa20(active0, 0x10001100000000L, active1, 0L);
      case 86:
        return jjMoveStringLiteralDfa20(active0, 0x40000000L, active1, 0L);
      case 89:
        if ((active0 & 0x400L) != 0L) {
          jjmatchedKind = 10;
          jjmatchedPos = 1;
        }
        break;
      case 97:
        return jjMoveStringLiteralDfa20(active0, 0x89008025104000L, active1, 0L);
      case 101:
        return jjMoveStringLiteralDfa20(active0, 0x42000040020L, active1, 0L);
      case 102:
        return jjMoveStringLiteralDfa20(active0, 0x2000L, active1, 0L);
      case 104:
        return jjMoveStringLiteralDfa20(active0, 0x40L, active1, 0L);
      case 105:
        return jjMoveStringLiteralDfa20(active0, 0x21a001a001100L, active1, 0L);
      case 107:
        return jjMoveStringLiteralDfa20(active0, 0x800L, active1, 0L);
      case 110:
        return jjMoveStringLiteralDfa20(active0, 0x800000200000L, active1, 0L);
      case 111:
        return jjMoveStringLiteralDfa20(active0, 0x124214680808000L, active1, 0L);
      case 112:
        return jjMoveStringLiteralDfa20(active0, 0x40000000010000L, active1, 0L);
      case 114:
        if ((active0 & 0x400000L) != 0L) {
          jjmatchedKind = 22;
          jjmatchedPos = 1;
        }
        return jjMoveStringLiteralDfa20(active0, 0x80280L, active1, 0L);
      case 115:
        if ((active0 & 0x800000000L) != 0L) {
          jjmatchedKind = 35;
          jjmatchedPos = 1;
        }
        return jjMoveStringLiteralDfa20(active0, 0x20000L, active1, 0L);
      case 116:
        return jjMoveStringLiteralDfa20(active0, 0x400000000000L, active1, 0L);
      case 117:
        return jjMoveStringLiteralDfa20(active0, 0x10001100000000L, active1, 0L);
      case 118:
        return jjMoveStringLiteralDfa20(active0, 0x40000000L, active1, 0L);
      case 121:
        if ((active0 & 0x400L) != 0L) {
          jjmatchedKind = 10;
          jjmatchedPos = 1;
        }
        break;
      default:
        break;
    }
    return jjMoveNfa0(0, 1);
  }

  private int jjMoveStringLiteralDfa100(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return jjMoveNfa0(0, 9);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 9);
    }
    switch (curChar) {
      case 68:
        if ((active0 & 0x80000000000L) != 0L) {
          jjmatchedKind = 43;
          jjmatchedPos = 10;
        }
        break;
      case 100:
        if ((active0 & 0x80000000000L) != 0L) {
          jjmatchedKind = 43;
          jjmatchedPos = 10;
        }
        break;
      default:
        break;
    }
    return jjMoveNfa0(0, 10);
  }

  private int jjMoveStringLiteralDfa20(long old0, long active0, long old1, long active1) {
    if (((active0 &= old0) | (active1 &= old1)) == 0L) {
      return jjMoveNfa0(0, 1);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 1);
    }
    switch (curChar) {
      case 65:
        return jjMoveStringLiteralDfa30(active0, 0x10402000000000L);
      case 66:
        return jjMoveStringLiteralDfa30(active0, 0x4000L);
      case 67:
        if ((active0 & 0x20000L) != 0L) {
          jjmatchedKind = 17;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x40000000000L);
      case 68:
        if ((active0 & 0x200000L) != 0L) {
          jjmatchedKind = 21;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x100800000000200L);
      case 69:
        return jjMoveStringLiteralDfa30(active0, 0x40L);
      case 70:
        return jjMoveStringLiteralDfa30(active0, 0x2000L);
      case 71:
        if ((active0 & 0x40000000L) != 0L) {
          jjmatchedKind = 30;
          jjmatchedPos = 2;
        }
        break;
      case 73:
        return jjMoveStringLiteralDfa30(active0, 0x800L);
      case 75:
        return jjMoveStringLiteralDfa30(active0, 0x2000000000000L);
      case 76:
        return jjMoveStringLiteralDfa30(active0, 0x81000100020L);
      case 77:
        if ((active0 & 0x100000000L) != 0L) {
          jjmatchedKind = 32;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0xa001000L);
      case 78:
        if ((active0 & 0x10000000L) != 0L) {
          jjmatchedKind = 28;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x224000000000L);
      case 79:
        return jjMoveStringLiteralDfa30(active0, 0x80L);
      case 80:
        return jjMoveStringLiteralDfa30(active0, 0x40000000000000L);
      case 82:
        return jjMoveStringLiteralDfa30(active0, 0x8000L);
      case 83:
        return jjMoveStringLiteralDfa30(active0, 0x40000L);
      case 84:
        if ((active0 & 0x800000L) != 0L) {
          jjmatchedKind = 23;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x9100005010000L);
      case 85:
        return jjMoveStringLiteralDfa30(active0, 0x10080080000L);
      case 86:
        return jjMoveStringLiteralDfa30(active0, 0x100L);
      case 87:
        if ((active0 & 0x4000000000000L) != 0L) {
          jjmatchedKind = 50;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x20000000000000L);
      case 88:
        if ((active0 & 0x20000000L) != 0L) {
          jjmatchedKind = 29;
          jjmatchedPos = 2;
        }
        break;
      case 89:
        if ((active0 & 0x8000000000L) != 0L) {
          jjmatchedKind = 39;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x80000000000000L);
      case 95:
        return jjMoveStringLiteralDfa30(active0, 0x600000000L);
      case 97:
        return jjMoveStringLiteralDfa30(active0, 0x10402000000000L);
      case 98:
        return jjMoveStringLiteralDfa30(active0, 0x4000L);
      case 99:
        if ((active0 & 0x20000L) != 0L) {
          jjmatchedKind = 17;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x40000000000L);
      case 100:
        if ((active0 & 0x200000L) != 0L) {
          jjmatchedKind = 21;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x100800000000200L);
      case 101:
        return jjMoveStringLiteralDfa30(active0, 0x40L);
      case 102:
        return jjMoveStringLiteralDfa30(active0, 0x2000L);
      case 103:
        if ((active0 & 0x40000000L) != 0L) {
          jjmatchedKind = 30;
          jjmatchedPos = 2;
        }
        break;
      case 105:
        return jjMoveStringLiteralDfa30(active0, 0x800L);
      case 107:
        return jjMoveStringLiteralDfa30(active0, 0x2000000000000L);
      case 108:
        return jjMoveStringLiteralDfa30(active0, 0x81000100020L);
      case 109:
        if ((active0 & 0x100000000L) != 0L) {
          jjmatchedKind = 32;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0xa001000L);
      case 110:
        if ((active0 & 0x10000000L) != 0L) {
          jjmatchedKind = 28;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x224000000000L);
      case 111:
        return jjMoveStringLiteralDfa30(active0, 0x80L);
      case 112:
        return jjMoveStringLiteralDfa30(active0, 0x40000000000000L);
      case 114:
        return jjMoveStringLiteralDfa30(active0, 0x8000L);
      case 115:
        return jjMoveStringLiteralDfa30(active0, 0x40000L);
      case 116:
        if ((active0 & 0x800000L) != 0L) {
          jjmatchedKind = 23;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x9100005010000L);
      case 117:
        return jjMoveStringLiteralDfa30(active0, 0x10080080000L);
      case 118:
        return jjMoveStringLiteralDfa30(active0, 0x100L);
      case 119:
        if ((active0 & 0x4000000000000L) != 0L) {
          jjmatchedKind = 50;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x20000000000000L);
      case 120:
        if ((active0 & 0x20000000L) != 0L) {
          jjmatchedKind = 29;
          jjmatchedPos = 2;
        }
        break;
      case 121:
        if ((active0 & 0x8000000000L) != 0L) {
          jjmatchedKind = 39;
          jjmatchedPos = 2;
        }
        return jjMoveStringLiteralDfa30(active0, 0x80000000000000L);
      default:
        break;
    }
    return jjMoveNfa0(0, 2);
  }

  private int jjMoveStringLiteralDfa30(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return jjMoveNfa0(0, 2);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 2);
    }
    switch (curChar) {
      case 65:
        return jjMoveStringLiteralDfa40(active0, 0x100000000000000L);
      case 67:
        if ((active0 & 0x40000L) != 0L) {
          jjmatchedKind = 18;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x1000000000000L);
      case 69:
        if ((active0 & 0x80000L) != 0L) {
          jjmatchedKind = 19;
          jjmatchedPos = 3;
        } else if ((active0 & 0x1000000L) != 0L) {
          jjmatchedKind = 24;
          jjmatchedPos = 3;
        } else if ((active0 & 0x2000000000000L) != 0L) {
          jjmatchedKind = 49;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x6800000e004220L);
      case 70:
        return jjMoveStringLiteralDfa40(active0, 0x400000000L);
      case 72:
        if ((active0 & 0x100000000000L) != 0L) {
          jjmatchedKind = 44;
          jjmatchedPos = 3;
        }
        break;
      case 73:
        return jjMoveStringLiteralDfa40(active0, 0x11000L);
      case 76:
        if ((active0 & 0x1000000000L) != 0L) {
          jjmatchedKind = 36;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x80000000000L);
      case 77:
        return jjMoveStringLiteralDfa40(active0, 0x8000L);
      case 78:
        return jjMoveStringLiteralDfa40(active0, 0x80000000L);
      case 79:
        return jjMoveStringLiteralDfa40(active0, 0x80040000000100L);
      case 80:
        return jjMoveStringLiteralDfa40(active0, 0x800L);
      case 82:
        if ((active0 & 0x2000000000L) != 0L) {
          jjmatchedKind = 37;
          jjmatchedPos = 3;
        } else if ((active0 & 0x10000000000L) != 0L) {
          jjmatchedKind = 40;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x10400000000040L);
      case 83:
        if ((active0 & 0x800000000000L) != 0L) {
          jjmatchedKind = 47;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x102000L);
      case 84:
        return jjMoveStringLiteralDfa40(active0, 0x204000000000L);
      case 85:
        return jjMoveStringLiteralDfa40(active0, 0x20000000080L);
      case 86:
        return jjMoveStringLiteralDfa40(active0, 0x200000000L);
      case 97:
        return jjMoveStringLiteralDfa40(active0, 0x100000000000000L);
      case 99:
        if ((active0 & 0x40000L) != 0L) {
          jjmatchedKind = 18;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x1000000000000L);
      case 101:
        if ((active0 & 0x80000L) != 0L) {
          jjmatchedKind = 19;
          jjmatchedPos = 3;
        } else if ((active0 & 0x1000000L) != 0L) {
          jjmatchedKind = 24;
          jjmatchedPos = 3;
        } else if ((active0 & 0x2000000000000L) != 0L) {
          jjmatchedKind = 49;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x6800000e004220L);
      case 102:
        return jjMoveStringLiteralDfa40(active0, 0x400000000L);
      case 104:
        if ((active0 & 0x100000000000L) != 0L) {
          jjmatchedKind = 44;
          jjmatchedPos = 3;
        }
        break;
      case 105:
        return jjMoveStringLiteralDfa40(active0, 0x11000L);
      case 108:
        if ((active0 & 0x1000000000L) != 0L) {
          jjmatchedKind = 36;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x80000000000L);
      case 109:
        return jjMoveStringLiteralDfa40(active0, 0x8000L);
      case 110:
        return jjMoveStringLiteralDfa40(active0, 0x80000000L);
      case 111:
        return jjMoveStringLiteralDfa40(active0, 0x80040000000100L);
      case 112:
        return jjMoveStringLiteralDfa40(active0, 0x800L);
      case 114:
        if ((active0 & 0x2000000000L) != 0L) {
          jjmatchedKind = 37;
          jjmatchedPos = 3;
        } else if ((active0 & 0x10000000000L) != 0L) {
          jjmatchedKind = 40;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x10400000000040L);
      case 115:
        if ((active0 & 0x800000000000L) != 0L) {
          jjmatchedKind = 47;
          jjmatchedPos = 3;
        }
        return jjMoveStringLiteralDfa40(active0, 0x102000L);
      case 116:
        return jjMoveStringLiteralDfa40(active0, 0x204000000000L);
      case 117:
        return jjMoveStringLiteralDfa40(active0, 0x20000000080L);
      case 118:
        return jjMoveStringLiteralDfa40(active0, 0x200000000L);
      default:
        break;
    }
    return jjMoveNfa0(0, 3);
  }

  private int jjMoveStringLiteralDfa40(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return jjMoveNfa0(0, 3);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 3);
    }
    switch (curChar) {
      case 65:
        return jjMoveStringLiteralDfa50(active0, 0x200200008000L);
      case 67:
        return jjMoveStringLiteralDfa50(active0, 0x20L);
      case 68:
        return jjMoveStringLiteralDfa50(active0, 0x8000000000000L);
      case 69:
        if ((active0 & 0x40L) != 0L) {
          jjmatchedKind = 6;
          jjmatchedPos = 4;
        } else if ((active0 & 0x100000L) != 0L) {
          jjmatchedKind = 20;
          jjmatchedPos = 4;
        }
        return jjMoveStringLiteralDfa50(active0, 0x2000L);
      case 70:
        return jjMoveStringLiteralDfa50(active0, 0x80000000000000L);
      case 72:
        if ((active0 & 0x4000000000L) != 0L) {
          jjmatchedKind = 38;
          jjmatchedPos = 4;
        }
        return jjMoveStringLiteralDfa50(active0, 0x1000000000000L);
      case 73:
        return jjMoveStringLiteralDfa50(active0, 0x80000000000L);
      case 76:
        if ((active0 & 0x4000L) != 0L) {
          jjmatchedKind = 14;
          jjmatchedPos = 4;
        }
        break;
      case 78:
        return jjMoveStringLiteralDfa50(active0, 0x40000000000L);
      case 79:
        return jjMoveStringLiteralDfa50(active0, 0x402010000L);
      case 80:
        if ((active0 & 0x80L) != 0L) {
          jjmatchedKind = 7;
          jjmatchedPos = 4;
        }
        return jjMoveStringLiteralDfa50(active0, 0x800L);
      case 82:
        if ((active0 & 0x200L) != 0L) {
          jjmatchedKind = 9;
          jjmatchedPos = 4;
        } else if ((active0 & 0x20000000000000L) != 0L) {
          jjmatchedKind = 53;
          jjmatchedPos = 4;
        } else if ((active0 & 0x40000000000000L) != 0L) {
          jjmatchedKind = 54;
          jjmatchedPos = 4;
        }
        break;
      case 83:
        return jjMoveStringLiteralDfa50(active0, 0x8000000L);
      case 84:
        if ((active0 & 0x100L) != 0L) {
          jjmatchedKind = 8;
          jjmatchedPos = 4;
        } else if ((active0 & 0x1000L) != 0L) {
          jjmatchedKind = 12;
          jjmatchedPos = 4;
        } else if ((active0 & 0x80000000L) != 0L) {
          jjmatchedKind = 31;
          jjmatchedPos = 4;
        }
        return jjMoveStringLiteralDfa50(active0, 0x110420004000000L);
      case 97:
        return jjMoveStringLiteralDfa50(active0, 0x200200008000L);
      case 99:
        return jjMoveStringLiteralDfa50(active0, 0x20L);
      case 100:
        return jjMoveStringLiteralDfa50(active0, 0x8000000000000L);
      case 101:
        if ((active0 & 0x40L) != 0L) {
          jjmatchedKind = 6;
          jjmatchedPos = 4;
        } else if ((active0 & 0x100000L) != 0L) {
          jjmatchedKind = 20;
          jjmatchedPos = 4;
        }
        return jjMoveStringLiteralDfa50(active0, 0x2000L);
      case 102:
        return jjMoveStringLiteralDfa50(active0, 0x80000000000000L);
      case 104:
        if ((active0 & 0x4000000000L) != 0L) {
          jjmatchedKind = 38;
          jjmatchedPos = 4;
        }
        return jjMoveStringLiteralDfa50(active0, 0x1000000000000L);
      case 105:
        return jjMoveStringLiteralDfa50(active0, 0x80000000000L);
      case 108:
        if ((active0 & 0x4000L) != 0L) {
          jjmatchedKind = 14;
          jjmatchedPos = 4;
        }
        break;
      case 110:
        return jjMoveStringLiteralDfa50(active0, 0x40000000000L);
      case 111:
        return jjMoveStringLiteralDfa50(active0, 0x402010000L);
      case 112:
        if ((active0 & 0x80L) != 0L) {
          jjmatchedKind = 7;
          jjmatchedPos = 4;
        }
        return jjMoveStringLiteralDfa50(active0, 0x800L);
      case 114:
        if ((active0 & 0x200L) != 0L) {
          jjmatchedKind = 9;
          jjmatchedPos = 4;
        } else if ((active0 & 0x20000000000000L) != 0L) {
          jjmatchedKind = 53;
          jjmatchedPos = 4;
        } else if ((active0 & 0x40000000000000L) != 0L) {
          jjmatchedKind = 54;
          jjmatchedPos = 4;
        }
        break;
      case 115:
        return jjMoveStringLiteralDfa50(active0, 0x8000000L);
      case 116:
        if ((active0 & 0x100L) != 0L) {
          jjmatchedKind = 8;
          jjmatchedPos = 4;
        } else if ((active0 & 0x1000L) != 0L) {
          jjmatchedKind = 12;
          jjmatchedPos = 4;
        } else if ((active0 & 0x80000000L) != 0L) {
          jjmatchedKind = 31;
          jjmatchedPos = 4;
        }
        return jjMoveStringLiteralDfa50(active0, 0x110420004000000L);
      default:
        break;
    }
    return jjMoveNfa0(0, 4);
  }

  private int jjMoveStringLiteralDfa50(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return jjMoveNfa0(0, 4);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 4);
    }
    switch (curChar) {
      case 68:
        if ((active0 & 0x40000000000L) != 0L) {
          jjmatchedKind = 42;
          jjmatchedPos = 5;
        }
        break;
      case 69:
        if ((active0 & 0x20000000000L) != 0L) {
          jjmatchedKind = 41;
          jjmatchedPos = 5;
        } else if ((active0 & 0x100000000000000L) != 0L) {
          jjmatchedKind = 56;
          jjmatchedPos = 5;
        }
        return jjMoveStringLiteralDfa60(active0, 0x11000000000000L);
      case 70:
        return jjMoveStringLiteralDfa60(active0, 0x2000000L);
      case 73:
        return jjMoveStringLiteralDfa60(active0, 0x8200004000800L);
      case 76:
        return jjMoveStringLiteralDfa60(active0, 0x200000000L);
      case 78:
        return jjMoveStringLiteralDfa60(active0, 0x10000L);
      case 82:
        return jjMoveStringLiteralDfa60(active0, 0x400000000L);
      case 83:
        if ((active0 & 0x400000000000L) != 0L) {
          jjmatchedKind = 46;
          jjmatchedPos = 5;
        }
        return jjMoveStringLiteralDfa60(active0, 0x80000000000L);
      case 84:
        if ((active0 & 0x20L) != 0L) {
          jjmatchedKind = 5;
          jjmatchedPos = 5;
        } else if ((active0 & 0x2000L) != 0L) {
          jjmatchedKind = 13;
          jjmatchedPos = 5;
        } else if ((active0 & 0x8000L) != 0L) {
          jjmatchedKind = 15;
          jjmatchedPos = 5;
        }
        return jjMoveStringLiteralDfa60(active0, 0x8000000L);
      case 87:
        return jjMoveStringLiteralDfa60(active0, 0x80000000000000L);
      case 100:
        if ((active0 & 0x40000000000L) != 0L) {
          jjmatchedKind = 42;
          jjmatchedPos = 5;
        }
        break;
      case 101:
        if ((active0 & 0x20000000000L) != 0L) {
          jjmatchedKind = 41;
          jjmatchedPos = 5;
        } else if ((active0 & 0x100000000000000L) != 0L) {
          jjmatchedKind = 56;
          jjmatchedPos = 5;
        }
        return jjMoveStringLiteralDfa60(active0, 0x11000000000000L);
      case 102:
        return jjMoveStringLiteralDfa60(active0, 0x2000000L);
      case 105:
        return jjMoveStringLiteralDfa60(active0, 0x8200004000800L);
      case 108:
        return jjMoveStringLiteralDfa60(active0, 0x200000000L);
      case 110:
        return jjMoveStringLiteralDfa60(active0, 0x10000L);
      case 114:
        return jjMoveStringLiteralDfa60(active0, 0x400000000L);
      case 115:
        if ((active0 & 0x400000000000L) != 0L) {
          jjmatchedKind = 46;
          jjmatchedPos = 5;
        }
        return jjMoveStringLiteralDfa60(active0, 0x80000000000L);
      case 116:
        if ((active0 & 0x20L) != 0L) {
          jjmatchedKind = 5;
          jjmatchedPos = 5;
        } else if ((active0 & 0x2000L) != 0L) {
          jjmatchedKind = 13;
          jjmatchedPos = 5;
        } else if ((active0 & 0x8000L) != 0L) {
          jjmatchedKind = 15;
          jjmatchedPos = 5;
        }
        return jjMoveStringLiteralDfa60(active0, 0x8000000L);
      case 119:
        return jjMoveStringLiteralDfa60(active0, 0x80000000000000L);
      default:
        break;
    }
    return jjMoveNfa0(0, 5);
  }

  private int jjMoveStringLiteralDfa60(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return jjMoveNfa0(0, 5);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 5);
    }
    switch (curChar) {
      case 65:
        return jjMoveStringLiteralDfa70(active0, 0x8000000L);
      case 68:
        return jjMoveStringLiteralDfa70(active0, 0x2000000L);
      case 69:
        return jjMoveStringLiteralDfa70(active0, 0x80080000000000L);
      case 70:
        return jjMoveStringLiteralDfa70(active0, 0x8000000000000L);
      case 77:
        return jjMoveStringLiteralDfa70(active0, 0x404000000L);
      case 78:
        return jjMoveStringLiteralDfa70(active0, 0x200000000800L);
      case 82:
        if ((active0 & 0x10000000000000L) != 0L) {
          jjmatchedKind = 52;
          jjmatchedPos = 6;
        }
        break;
      case 83:
        if ((active0 & 0x10000L) != 0L) {
          jjmatchedKind = 16;
          jjmatchedPos = 6;
        } else if ((active0 & 0x1000000000000L) != 0L) {
          jjmatchedKind = 48;
          jjmatchedPos = 6;
        }
        break;
      case 85:
        return jjMoveStringLiteralDfa70(active0, 0x200000000L);
      case 97:
        return jjMoveStringLiteralDfa70(active0, 0x8000000L);
      case 100:
        return jjMoveStringLiteralDfa70(active0, 0x2000000L);
      case 101:
        return jjMoveStringLiteralDfa70(active0, 0x80080000000000L);
      case 102:
        return jjMoveStringLiteralDfa70(active0, 0x8000000000000L);
      case 109:
        return jjMoveStringLiteralDfa70(active0, 0x404000000L);
      case 110:
        return jjMoveStringLiteralDfa70(active0, 0x200000000800L);
      case 114:
        if ((active0 & 0x10000000000000L) != 0L) {
          jjmatchedKind = 52;
          jjmatchedPos = 6;
        }
        break;
      case 115:
        if ((active0 & 0x10000L) != 0L) {
          jjmatchedKind = 16;
          jjmatchedPos = 6;
        } else if ((active0 & 0x1000000000000L) != 0L) {
          jjmatchedKind = 48;
          jjmatchedPos = 6;
        }
        break;
      case 117:
        return jjMoveStringLiteralDfa70(active0, 0x200000000L);
      default:
        break;
    }
    return jjMoveNfa0(0, 6);
  }

  private int jjMoveStringLiteralDfa70(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return jjMoveNfa0(0, 6);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 6);
    }
    switch (curChar) {
      case 65:
        return jjMoveStringLiteralDfa80(active0, 0x402000000L);
      case 67:
        return jjMoveStringLiteralDfa80(active0, 0x80000000000L);
      case 69:
        if ((active0 & 0x4000000L) != 0L) {
          jjmatchedKind = 26;
          jjmatchedPos = 7;
        }
        return jjMoveStringLiteralDfa80(active0, 0x80000200000000L);
      case 70:
        if ((active0 & 0x8000000000000L) != 0L) {
          jjmatchedKind = 51;
          jjmatchedPos = 7;
        }
        break;
      case 71:
        if ((active0 & 0x800L) != 0L) {
          jjmatchedKind = 11;
          jjmatchedPos = 7;
        }
        break;
      case 77:
        return jjMoveStringLiteralDfa80(active0, 0x8000000L);
      case 83:
        if ((active0 & 0x200000000000L) != 0L) {
          jjmatchedKind = 45;
          jjmatchedPos = 7;
        }
        break;
      case 97:
        return jjMoveStringLiteralDfa80(active0, 0x402000000L);
      case 99:
        return jjMoveStringLiteralDfa80(active0, 0x80000000000L);
      case 101:
        if ((active0 & 0x4000000L) != 0L) {
          jjmatchedKind = 26;
          jjmatchedPos = 7;
        }
        return jjMoveStringLiteralDfa80(active0, 0x80000200000000L);
      case 102:
        if ((active0 & 0x8000000000000L) != 0L) {
          jjmatchedKind = 51;
          jjmatchedPos = 7;
        }
        break;
      case 103:
        if ((active0 & 0x800L) != 0L) {
          jjmatchedKind = 11;
          jjmatchedPos = 7;
        }
        break;
      case 109:
        return jjMoveStringLiteralDfa80(active0, 0x8000000L);
      case 115:
        if ((active0 & 0x200000000000L) != 0L) {
          jjmatchedKind = 45;
          jjmatchedPos = 7;
        }
        break;
      default:
        break;
    }
    return jjMoveNfa0(0, 7);
  }

  private int jjMoveStringLiteralDfa80(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return jjMoveNfa0(0, 7);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 7);
    }
    switch (curChar) {
      case 75:
        if ((active0 & 0x80000000000000L) != 0L) {
          jjmatchedKind = 55;
          jjmatchedPos = 8;
        }
        break;
      case 79:
        return jjMoveStringLiteralDfa90(active0, 0x80000000000L);
      case 80:
        if ((active0 & 0x8000000L) != 0L) {
          jjmatchedKind = 27;
          jjmatchedPos = 8;
        }
        break;
      case 83:
        if ((active0 & 0x200000000L) != 0L) {
          jjmatchedKind = 33;
          jjmatchedPos = 8;
        }
        break;
      case 84:
        if ((active0 & 0x400000000L) != 0L) {
          jjmatchedKind = 34;
          jjmatchedPos = 8;
        }
        break;
      case 89:
        if ((active0 & 0x2000000L) != 0L) {
          jjmatchedKind = 25;
          jjmatchedPos = 8;
        }
        break;
      case 107:
        if ((active0 & 0x80000000000000L) != 0L) {
          jjmatchedKind = 55;
          jjmatchedPos = 8;
        }
        break;
      case 111:
        return jjMoveStringLiteralDfa90(active0, 0x80000000000L);
      case 112:
        if ((active0 & 0x8000000L) != 0L) {
          jjmatchedKind = 27;
          jjmatchedPos = 8;
        }
        break;
      case 115:
        if ((active0 & 0x200000000L) != 0L) {
          jjmatchedKind = 33;
          jjmatchedPos = 8;
        }
        break;
      case 116:
        if ((active0 & 0x400000000L) != 0L) {
          jjmatchedKind = 34;
          jjmatchedPos = 8;
        }
        break;
      case 121:
        if ((active0 & 0x2000000L) != 0L) {
          jjmatchedKind = 25;
          jjmatchedPos = 8;
        }
        break;
      default:
        break;
    }
    return jjMoveNfa0(0, 8);
  }

  private int jjMoveStringLiteralDfa90(long old0, long active0) {
    if (((active0 &= old0)) == 0L) {
      return jjMoveNfa0(0, 8);
    }
    try {
      curChar = inputStream.readChar();
    } catch (java.io.IOException e) {
      return jjMoveNfa0(0, 8);
    }
    switch (curChar) {
      case 78:
        return jjMoveStringLiteralDfa100(active0, 0x80000000000L);
      case 110:
        return jjMoveStringLiteralDfa100(active0, 0x80000000000L);
      default:
        break;
    }
    return jjMoveNfa0(0, 9);
  }

  private void reInitRounds() {
    int i;
    jjround = 0x80000001;
    for (i = 22; i-- > 0;) {
      jjrounds[i] = 0x80000000;
    }
  }
}
