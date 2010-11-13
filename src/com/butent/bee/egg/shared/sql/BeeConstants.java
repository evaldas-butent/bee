package com.butent.bee.egg.shared.sql;

public final class BeeConstants {

  public enum DataTypes {
    BOOLEAN, INTEGER, LONG, DOUBLE, NUMERIC, CHAR, STRING
  }
  public enum Keywords {
    NOTNULL, UNIQUE, PRIMARY, REFERENCES, CASCADE, SETNULL
  }

  public static final String BEE_TABLE = "bee_Tables";
  public static final String BEE_TABLE_NAME = "TableName";
  public static final String BEE_TABLE_IDCOLUMN = "IdColumn";
  public static final String BEE_TABLE_LOCKCOLUMN = "LockColumn";

  private BeeConstants() {
  }
}
