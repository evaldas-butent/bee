package com.butent.bee.egg.shared;

public abstract class BeeConst {
  public static final String MYSQL = "MySql";
  public static final String MSSQL = "MsSql";
  public static final String ORACLE = "Oracle";

  public static final String[] DS_TYPES = { MYSQL, MSSQL, ORACLE };

  public static final String NO = "no";
  public static final String YES = "yes";

  public static final String UNKNOWN = "unknown";
  public static final String EMPTY = "(empty)";
  public static final String DEFAULT = "default";

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
  public static final String STRING_OPEN_BRACKET = "[";
  public static final String STRING_CLOSE_BRACKET = "]";

  public static final String STRING_FALSE = Boolean.toString(false);
  public static final String STRING_TRUE = Boolean.toString(true);

  public static final char CHAR_SPACE = ' ';
  public static final char CHAR_ZERO = '0';

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

  public static final boolean validDsType(String tp) {
    boolean ok = false;

    if (tp == null || tp.isEmpty())
      return ok;

    for (int i = 0; i < DS_TYPES.length; i++)
      if (tp.equals(DS_TYPES[i])) {
        ok = true;
        break;
      }

    return ok;
  }
}
