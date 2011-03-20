package com.butent.bee.shared;

public class BeeConst {
  public static final String MYSQL = "MySql";
  public static final String MSSQL = "MsSql";
  public static final String ORACLE = "Oracle";
  public static final String PGSQL = "PostgreSql";

  public static final String[] DS_TYPES = {MYSQL, MSSQL, ORACLE, PGSQL};

  public static final String NO = "no";
  public static final String YES = "yes";

  public static final String UNKNOWN = "unknown";
  public static final String EMPTY = "(empty)";
  public static final String DEFAULT = "default";
  public static final String ERROR = "error";
  public static final String NULL = "null";

  public static final String DEFAULT_LIST_SEPARATOR = ", ";
  public static final String DEFAULT_VALUE_SEPARATOR = "=";
  public static final String DEFAULT_OPTION_SEPARATOR = ";";
  public static final String DEFAULT_ROW_SEPARATOR = ";";
  public static final String DEFAULT_PROPERTY_SEPARATOR = ".";
  public static final String DEFAULT_PROGRESS_SEPARATOR = "/";

  public static final String ELLIPSIS = "...";

  public static final String STRING_EMPTY = "";
  public static final String STRING_SPACE = " ";
  public static final String STRING_ZERO = "0";
  public static final String STRING_COMMA = ",";
  public static final String STRING_POINT = ".";
  public static final String STRING_LEFT_BRACKET = "[";
  public static final String STRING_RIGHT_BRACKET = "]";
  public static final String STRING_LEFT_PARENTHESIS = "(";
  public static final String STRING_RIGHT_PARENTHESIS = ")";
  public static final String STRING_EQ = "=";
  public static final String STRING_EOL = "\n";
  public static final String STRING_MINUS = "-";
  public static final String STRING_PLUS = "+";
  public static final String STRING_ALL = "*";

  public static final String STRING_FALSE = Boolean.toString(false);
  public static final String STRING_TRUE = Boolean.toString(true);

  public static final int INT_ERROR = -1;
  public static final int INT_FALSE = 0;
  public static final int INT_TRUE = 1;

  public static final char CHAR_SPACE = ' ';
  public static final char CHAR_UNDER = '_';
  public static final char CHAR_ZERO = '0';
  public static final char CHAR_NINE = '9';
  public static final char CHAR_POINT = '.';
  public static final char CHAR_COMMA = ',';
  public static final char CHAR_EOL = '\n';
  public static final char CHAR_EQ = '=';
  public static final char CHAR_AMP = '&';
  public static final char CHAR_LT = '<';
  public static final char CHAR_GT = '>';
  public static final char CHAR_QUOT = '"';
  public static final char CHAR_APOS = '\'';
  public static final char CHAR_MINUS = '-';
  public static final char CHAR_PLUS = '+';
  public static final String CHAR_FALSE = "fFnN0";
  public static final String CHAR_TRUE = "tTyY1";

  public static final int SIZE_UNKNOWN = -1;
  public static final int TIME_UNKNOWN = -1;
  public static final int SELECTION_UNKNOWN = -1;
  public static final int INDEX_UNKNOWN = -1;

  public static final int COMPARE_LESS = -1;
  public static final int COMPARE_EQUAL = 0;
  public static final int COMPARE_MORE = 1;

  public static final String XML_DEFAULT_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  public static final String CLOSE_CURSORS_AT_COMMIT = "close cursors at commit";
  public static final String HOLD_CURSORS_OVER_COMMIT = "hold cursors over commit";

  public static final String FETCH_FORWARD = "fetch forward";
  public static final String FETCH_REVERSE = "fetch reverse";
  public static final String FETCH_UNKNOWN = "fetch unknown";

  public static final String TYPE_FORWARD_ONLY = "forward only";
  public static final String TYPE_SCROLL_INSENSITIVE = "scroll insensitive";
  public static final String TYPE_SCROLL_SENSITIVE = "scroll sensitive";

  public static final String CONCUR_READ_ONLY = "concur read only";
  public static final String CONCUR_UPDATABLE = "concur updatable";

  public static final String TRANSACTION_NONE = "transaction none";
  public static final String TRANSACTION_READ_COMMITTED = "transaction read committed";
  public static final String TRANSACTION_READ_UNCOMMITTED = "transaction read uncommitted";
  public static final String TRANSACTION_REPEATABLE_READ = "transaction repeatable read";
  public static final String TRANSACTION_SERIALIZABLE = "transaction serializable";

  public static final String JDBC_RESULT_SET = "result set";
  public static final String JDBC_META_DATA = "meta data";
  public static final String JDBC_ROW_COUNT = "row count";
  public static final String JDBC_COLUMNS = "columns";

  public static final int STATE_UNKNOWN = 0;
  public static final int STATE_INITIALIZED = 1;
  public static final int STATE_OPEN = 2;
  public static final int STATE_CLOSED = 4;
  public static final int STATE_ERROR = 8;
  public static final int STATE_EXPIRED = 16;
  public static final int STATE_CANCELED = 32;
  public static final int STATE_CHANGED = 64;

  public static final String SERVER = "server";
  public static final String CLIENT = "client";

  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  public static final int[] EMPTY_INT_ARRAY = new int[0];

  public static final String HTML_NBSP = "&nbsp;";

  public static final int MAX_SCALE = 20;
  public static final double DOUBLE_ZERO = 0.0d; 
  public static final float FLOAT_ZERO = 0.0f; 

  private static String home = SERVER;
  
  public static String getDsType(String dsn) {
    if (dsn == null) {
      return null;
    }
    
    String name = dsn.trim().toLowerCase();
    if (name.isEmpty()) {
      return null;
    }
    char c = name.charAt(0);
    String tp;
    
    if (name.indexOf("my") == 0) {
      tp = MYSQL;
    } else if (c == 'm' && !name.contains("my")) {
      tp = MSSQL;
    } else if (c == 'o') {
      tp = ORACLE;
    } else if (c == 'p') {
      tp = PGSQL;
    } else if (name.contains("my")) {
      tp = MYSQL;
    } else if (name.contains("ms") || name.contains("micros")) {
      tp = BeeConst.MSSQL;
    } else if (name.contains("or")) {
      tp = BeeConst.ORACLE;
    } else if (name.contains("pg") || name.contains("postg")) {
      tp = BeeConst.PGSQL;
    } else {
      tp = null;
    }
    return tp;
  }
  
  public static boolean isClient() {
    return home.equals(CLIENT);
  }

  public static boolean isDefault(String s) {
    if (s == null) {
      return false;
    } else {
      return s.trim().equalsIgnoreCase(DEFAULT);
    }
  }

  public static boolean isError(int x) {
    return x == INT_ERROR;
  }
  
  public static boolean isFalse(char c) {
    return CHAR_FALSE.indexOf(c) >= 0;
  }

  public static boolean isServer() {
    return home.equals(SERVER);
  }

  public static boolean isTrue(char c) {
    return CHAR_TRUE.indexOf(c) >= 0;
  }

  public static void setClient() {
    home = CLIENT;
  }

  public static void setServer() {
    home = SERVER;
  }

  public static boolean validDsType(String tp) {
    boolean ok = false;
    if (tp == null || tp.isEmpty()) {
      return ok;
    }

    for (int i = 0; i < DS_TYPES.length; i++) {
      if (tp.equals(DS_TYPES[i])) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  public static String whereAmI() {
    if (isClient()) {
      return CLIENT;
    } else if (isServer()) {
      return SERVER;
    } else {
      return UNKNOWN;
    }
  }
  
  private BeeConst() {
  }
}
