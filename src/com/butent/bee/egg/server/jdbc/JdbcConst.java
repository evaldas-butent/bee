package com.butent.bee.egg.server.jdbc;

public abstract class JdbcConst {
  public static String ROW_ID = "row_id";

  public static String RESULT_SET_EMPTY = "result set empty";

  public static String rsRows(int cnt) {
    return "result set has " + String.valueOf(cnt) + " rows";
  }

  public static String rsColumns(int cnt) {
    return "result set has " + String.valueOf(cnt) + " columns";
  }

}
