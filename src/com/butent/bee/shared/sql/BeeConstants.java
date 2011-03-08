package com.butent.bee.shared.sql;

public final class BeeConstants {

  public enum DataType {
    BOOLEAN, INTEGER, LONG, DOUBLE, NUMERIC, CHAR, STRING, DATE, DATETIME
  }
  public enum Keyword {
    DB_NAME, DB_SCHEMA, DB_TABLES, DB_FOREIGNKEYS,
    DROP_TABLE, DROP_FOREIGNKEY, CREATE_INDEX, ADD_CONSTRAINT,
    PRIMARYKEY, FOREIGNKEY, CASCADE, SET_NULL, NOT_NULL,
    TEMPORARY, TEMPORARY_NAME, BITAND, IF
  }

  private BeeConstants() {
  }
}
