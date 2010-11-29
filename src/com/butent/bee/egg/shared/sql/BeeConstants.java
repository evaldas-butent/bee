package com.butent.bee.egg.shared.sql;

public final class BeeConstants {

  public enum DataTypes {
    BOOLEAN, INTEGER, LONG, FLOAT, DOUBLE, NUMERIC, CHAR, STRING
  }
  public enum Keywords {
    NOT_NULL, DB_TABLES, DB_FOREIGNKEYS, DROP_TABLE, DROP_FOREIGNKEY,
    CREATE_INDEX, ADD_CONSTRAINT, PRIMARYKEY, FOREIGNKEY, CASCADE, SET_NULL
  }

  private BeeConstants() {
  }
}
