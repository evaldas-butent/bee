package com.butent.bee.shared.sql;

public final class BeeConstants {

  public enum DataTypes {
    BOOLEAN, INTEGER, LONG, FLOAT, DOUBLE, NUMERIC, CHAR, STRING
  }
  public enum Keywords {
    DB_NAME, DB_SCHEMA, DB_TABLES, DB_FOREIGNKEYS,
    DROP_TABLE, DROP_FOREIGNKEY, CREATE_INDEX, ADD_CONSTRAINT,
    PRIMARYKEY, FOREIGNKEY, CASCADE, SET_NULL, NOT_NULL,
    TEMPORARY, TEMPORARY_NAME, BITAND, IF
  }

  private BeeConstants() {
  }
}
