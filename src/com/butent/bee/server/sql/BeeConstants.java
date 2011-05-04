package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;

/**
 * Contains constants lists, such as keywords, datatypes and empty values for 
 * each of them.
 */

public final class BeeConstants {

  /**
   * Contains a list of supported data types.
   */

  public enum DataType {
    BOOLEAN, INTEGER, LONG, DOUBLE, NUMERIC, CHAR, STRING, DATE, DATETIME;

    public Object getEmptyValue() {
      Object value = null;

      switch (this) {
        case BOOLEAN:
        case INTEGER:
        case LONG:
        case DOUBLE:
        case NUMERIC:
        case DATE:
        case DATETIME:
          value = 0;
          break;
        case CHAR:
        case STRING:
          value = "";
          break;
        default:
          Assert.unsupported();
          break;
      }
      return value;
    }
  }

  /**
   * Contains a list of SQL keywords used by the system.
   */

  public enum Keyword {
    DB_NAME, DB_SCHEMA, DB_TABLES, DB_FOREIGNKEYS,
    DROP_TABLE, DROP_FOREIGNKEY, CREATE_INDEX, ADD_CONSTRAINT, PRIMARYKEY, FOREIGNKEY,
    TEMPORARY, TEMPORARY_NAME, NOT_NULL, CASCADE, SET_NULL,
    BITAND, IF, CASE, CAST
  }

  public static final String FK_NAME = "fkName";
  public static final String FK_TABLE = "fkTable";
  public static final String FK_REF_TABLE = "fkRefTable";

  private BeeConstants() {
  }
}
