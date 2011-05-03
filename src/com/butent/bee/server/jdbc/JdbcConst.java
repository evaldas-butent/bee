package com.butent.bee.server.jdbc;

import java.sql.ResultSet;

/**
 * Contains constant expressions and values for JDBC connectivity parameters.
 */

public class JdbcConst {
  public static final String ROW_ID = "row_id";
  public static final String RESULT_SET_EMPTY = "result set empty";
  public static final String FEATURE_NOT_SUPPORTED = "feature not supported";

  public static final int UNKNOWN_RESULT_SET_TYPE = -1;
  public static final int UNKNOWN_CONCURRENCY = -1;
  public static final int UNKNOWN_HOLDABILITY = -1;
  public static final int UNKNOWN_FETCH_DIRECTION = -1;
  public static final int UNKNOWN_TRANSACTION_ISOLATION = -1;

  public static final int DEFAULT_RESULT_SET_TYPE = ResultSet.TYPE_FORWARD_ONLY;
  public static final int DEFAULT_CONCURRENCY = ResultSet.CONCUR_READ_ONLY;

  public static String rsColumns(int cnt) {
    return "result set has " + String.valueOf(cnt) + " columns";
  }

  public static String rsRows(int cnt) {
    return "result set has " + String.valueOf(cnt) + " rows";
  }

  private JdbcConst() {
  }
}
