package com.butent.bee.shared.utils;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;

import java.util.List;
import java.util.Stack;

public class Wildcards {
  public static class Pattern {
    private static final char CHAR_EXACT = '=';

    private final String expr;
    private final char wildcardAny;
    private final char wildcardOne;
    private final boolean sensitive;

    private final boolean containsWildcards;
    private final String[] tokens;
    private final boolean exact;

    private Pattern(String expr, char wildcardAny, char wildcardOne, boolean sensitive) {
      Assert.notEmpty(expr);
      this.wildcardAny = wildcardAny;
      this.wildcardOne = wildcardOne;
      this.sensitive = sensitive;

      if (expr.indexOf(wildcardAny) >= 0 || expr.indexOf(wildcardOne) >= 0) {
        this.expr = expr.trim();
        this.containsWildcards = true;
        this.tokens = tokenize();
        this.exact = false;
      } else if (BeeUtils.isPrefixOrSuffix(expr, CHAR_EXACT)) {
        this.expr = BeeUtils.removePrefixAndSuffix(expr, CHAR_EXACT);
        this.containsWildcards = false;
        this.tokens = null;
        this.exact = true;
      } else {
        this.expr = expr.trim();
        this.containsWildcards = false;
        this.tokens = null;
        this.exact = false;
      }
    }

    public String getExpr() {
      return expr;
    }

    public String[] getTokens() {
      return tokens;
    }

    public char getWildcardAny() {
      return wildcardAny;
    }

    public char getWildcardOne() {
      return wildcardOne;
    }

    public boolean hasWildcards() {
      return containsWildcards;
    }

    public boolean isExact() {
      return exact;
    }

    public boolean isSensitive() {
      return sensitive;
    }

    @Override
    public String toString() {
      return BeeUtils.concat(1, expr, sensitive ? "(sensitive)" : "", exact ? "(exact)" : "");
    }

    private String[] tokenize() {
      char[] arr = expr.toCharArray();
      String strAny = String.valueOf(wildcardAny);
      String strOne = String.valueOf(wildcardOne);

      List<String> lst = Lists.newArrayList();
      StringBuilder buffer = new StringBuilder();

      int i = 0;
      while (i < arr.length) {
        if (arr[i] == wildcardAny || arr[i] == wildcardOne) {
          if (buffer.length() > 0) {
            lst.add(buffer.toString());
            buffer.setLength(0);
          }

          if (arr[i] == wildcardOne) {
            lst.add(strOne);
          } else {
            for (int j = i + 1; j < arr.length; j++) {
              if (arr[j] == wildcardOne) {
                lst.add(strOne);
                i = j;
              } else if (arr[j] == wildcardAny) {
                i = j;
              } else {
                break;
              }
            }
            lst.add(strAny);
          }
        } else {
          buffer.append(arr[i]);
        }
        i++;
      }
      if (buffer.length() > 0) {
        lst.add(buffer.toString());
      }
      return lst.toArray(new String[lst.size()]);
    }
  }

  private static class DefaultPattern extends Pattern {
    private DefaultPattern(String expr) {
      this(expr, defaultCaseSensitivity);
    }

    private DefaultPattern(String expr, boolean sensitive) {
      super(expr, defaultAny, defaultOne, sensitive);
    }
  }

  private static class FsPattern extends Pattern {
    private FsPattern(String expr) {
      this(expr, fsCaseSensitivity);
    }

    private FsPattern(String expr, boolean sensitive) {
      super(expr, FS_ANY, FS_ONE, sensitive);
    }
  }

  private static class SqlPattern extends Pattern {
    private SqlPattern(String expr) {
      this(expr, sqlCaseSensitivity);
    }

    private SqlPattern(String expr, boolean sensitive) {
      super(expr, SQL_ANY, SQL_ONE, sensitive);
    }
  }

  private static final char SQL_ANY = '%';
  private static final char SQL_ONE = '_';
  private static boolean sqlCaseSensitivity = false;

  private static final char FS_ANY = '*';
  private static final char FS_ONE = '?';
  private static boolean fsCaseSensitivity = false;

  private static char defaultAny = FS_ANY;
  private static char defaultOne = FS_ONE;
  private static boolean defaultCaseSensitivity = false;

  public static char getDefaultAny() {
    return defaultAny;
  }

  public static char getDefaultOne() {
    return defaultOne;
  }

  public static Pattern getDefaultPattern(String expr) {
    return new DefaultPattern(expr);
  }

  public static Pattern getDefaultPattern(String expr, boolean sens) {
    return new DefaultPattern(expr, sens);
  }

  public static char getFsAny() {
    return FS_ANY;
  }

  public static char getFsOne() {
    return FS_ONE;
  }

  public static Pattern getFsPattern(String expr) {
    return new FsPattern(expr);
  }

  public static Pattern getFsPattern(String expr, boolean sens) {
    return new FsPattern(expr, sens);
  }

  public static Pattern getPattern(String expr, char any, char one, boolean sens) {
    return new Pattern(expr, any, one, sens);
  }

  public static char getSqlAny() {
    return SQL_ANY;
  }

  public static char getSqlOne() {
    return SQL_ONE;
  }

  public static Pattern getSqlPattern(String expr) {
    return new SqlPattern(expr);
  }

  public static Pattern getSqlPattern(String expr, boolean sens) {
    return new SqlPattern(expr, sens);
  }

  public static boolean hasDefaultWildcards(String expr) {
    if (expr == null) {
      return false;
    }
    return expr.indexOf(defaultAny) >= 0 || expr.indexOf(defaultOne) >= 0;
  }

  public static boolean hasFsWildcards(String expr) {
    if (expr == null) {
      return false;
    }
    return expr.indexOf(FS_ANY) >= 0 || expr.indexOf(FS_ONE) >= 0;
  }

  public static boolean hasSqlWildcards(String expr) {
    if (expr == null) {
      return false;
    }
    return expr.indexOf(SQL_ANY) >= 0 || expr.indexOf(SQL_ONE) >= 0;
  }

  public static boolean isDefaultAny(String expr) {
    return BeeUtils.containsOnly(expr, defaultAny);
  }

  public static boolean isDefaultCaseSensitive() {
    return defaultCaseSensitivity;
  }

  public static boolean isDefaultPattern(String expr) {
    return !BeeUtils.isEmpty(expr) && !isDefaultAny(expr);
  }

  public static boolean isFsAny(String expr) {
    return BeeUtils.containsOnly(expr, FS_ANY);
  }

  public static boolean isFsCaseSensitive() {
    return fsCaseSensitivity;
  }

  public static boolean isFsLike(String input, String expr) {
    return isLike(input, new FsPattern(expr));
  }

  public static boolean isFsLike(String input, String expr, boolean sensitive) {
    return isLike(input, new FsPattern(expr, sensitive));
  }

  public static boolean isFsPattern(String expr) {
    return !BeeUtils.isEmpty(expr) && !isFsAny(expr);
  }

  public static boolean isLike(String input, Pattern pattern) {
    Assert.notNull(pattern);
    if (BeeUtils.isEmpty(input)) {
      return false;
    }

    boolean sensitive = pattern.isSensitive();

    if (pattern.isExact()) {
      String xStr = pattern.getExpr();
      return sensitive ? input.equals(xStr) : BeeUtils.same(xStr, input);
    }
    if (!pattern.hasWildcards()) {
      String cStr = pattern.getExpr();
      return sensitive ? input.contains(cStr) : BeeUtils.context(cStr, input);
    }

    String[] tokens = pattern.getTokens();
    String strAny = String.valueOf(pattern.getWildcardAny());
    String strOne = String.valueOf(pattern.getWildcardOne());

    int tokIdx = 0;
    int inpIdx = 0;
    boolean anyChars = false;
    Stack<int[]> backtrack = new Stack<int[]>();

    do {
      if (backtrack.size() > 0) {
        int[] positions = backtrack.pop();
        tokIdx = positions[0];
        inpIdx = positions[1];
        anyChars = true;
      }

      while (tokIdx < tokens.length) {
        if (tokens[tokIdx].equals(strOne)) {
          inpIdx++;
          if (inpIdx > input.length()) {
            break;
          }
          anyChars = false;

        } else if (tokens[tokIdx].equals(strAny)) {
          anyChars = true;
          if (tokIdx == tokens.length - 1) {
            inpIdx = input.length();
          }

        } else {
          if (anyChars) {
            inpIdx = indexOfContext(sensitive, input, inpIdx, tokens[tokIdx]);
            if (inpIdx == -1) {
              break;
            }
            int repeat = indexOfContext(sensitive, input, inpIdx + 1, tokens[tokIdx]);
            if (repeat >= 0) {
              backtrack.push(new int[]{tokIdx, repeat});
            }
          } else {
            if (!isContext(sensitive, input, inpIdx, tokens[tokIdx])) {
              break;
            }
          }

          inpIdx += tokens[tokIdx].length();
          anyChars = false;
        }

        tokIdx++;
      }

      if (tokIdx == tokens.length && inpIdx == input.length()) {
        return true;
      }

    } while (backtrack.size() > 0);

    return false;
  }

  public static boolean isLike(String input, String expr) {
    return isLike(input, new DefaultPattern(expr));
  }

  public static boolean isLike(String input, String expr, boolean sensitive) {
    return isLike(input, new DefaultPattern(expr, sensitive));
  }

  public static boolean isSqlAny(String expr) {
    return BeeUtils.containsOnly(expr, SQL_ANY);
  }

  public static boolean isSqlCaseSensitive() {
    return sqlCaseSensitivity;
  }

  public static boolean isSqlLike(String input, String expr) {
    return isLike(input, new SqlPattern(expr));
  }

  public static boolean isSqlLike(String input, String expr, boolean sensitive) {
    return isLike(input, new SqlPattern(expr, sensitive));
  }

  public static boolean isSqlPattern(String expr) {
    return !BeeUtils.isEmpty(expr) && !isSqlAny(expr);
  }

  public static void setDefaultAny(char defaultAny) {
    Wildcards.defaultAny = defaultAny;
  }

  public static void setDefaultCaseSensitivity(boolean defaultCaseSensitivity) {
    Wildcards.defaultCaseSensitivity = defaultCaseSensitivity;
  }

  public static void setDefaultOne(char defaultOne) {
    Wildcards.defaultOne = defaultOne;
  }

  public static void setFsCaseSensitivity(boolean fsCaseSensitivity) {
    Wildcards.fsCaseSensitivity = fsCaseSensitivity;
  }

  public static void setSqlCaseSensitivity(boolean sqlCaseSensitivity) {
    Wildcards.sqlCaseSensitivity = sqlCaseSensitivity;
  }

  private static int indexOfContext(boolean sensitive, String src, int start, String ctxt) {
    int end = src.length() - ctxt.length();
    for (int i = start; i <= end; i++) {
      if (isContext(sensitive, src, i, ctxt)) {
        return i;
      }
    }
    return -1;
  }

  private static boolean isContext(boolean sensitive, String src, int start, String ctxt) {
    return src.regionMatches(!sensitive, start, ctxt, 0, ctxt.length());
  }

  private Wildcards() {
  }
}
