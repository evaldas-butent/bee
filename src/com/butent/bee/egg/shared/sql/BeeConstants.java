package com.butent.bee.egg.shared.sql;

public final class BeeConstants {

  public enum DataTypes {
    BOOLEAN, INTEGER, LONG, DOUBLE, NUMERIC, CHAR, STRING
  }
  public enum Keywords {
    NOT_NULL, GET_TABLES, DROP_TABLE, CREATE_INDEX, ADD_CONSTRAINT, PRIMARY, FOREIGN, CASCADE, SET_NULL
  }

  public static final String BEE_TABLE = "bee_Tables";
  public static final String BEE_TABLE_NAME = "TableName";
  public static final String BEE_TABLE_IDCOLUMN = "IdColumn";
  public static final String BEE_TABLE_LOCKCOLUMN = "LockColumn";

  private BeeConstants() {
  }
}
