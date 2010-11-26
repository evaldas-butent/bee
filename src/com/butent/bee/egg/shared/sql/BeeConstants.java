package com.butent.bee.egg.shared.sql;

public final class BeeConstants {

  public enum DataTypes {
    BOOLEAN, INTEGER, LONG, DOUBLE, NUMERIC, CHAR, STRING
  }
  public enum Keywords {
    NOT_NULL, GET_TABLES, DROP_TABLE, DROP_FOREIGN,
    CREATE_INDEX, ADD_CONSTRAINT, PRIMARY, FOREIGN, CASCADE, SET_NULL
  }

  private BeeConstants() {
  }
}
