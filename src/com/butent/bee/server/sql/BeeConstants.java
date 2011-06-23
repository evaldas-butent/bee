package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains constants lists, such as keywords, datatypes and empty values for each of them.
 */

public final class BeeConstants {

  /**
   * Contains a list of supported data types.
   */

  public enum DataType {
    BOOLEAN, INTEGER, LONG, DOUBLE, DECIMAL, CHAR, STRING, DATE, DATETIME;

    public Object getEmptyValue() {
      Object value = null;

      switch (this) {
        case BOOLEAN:
        case INTEGER:
        case LONG:
        case DOUBLE:
        case DECIMAL:
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

    public Object parse(String s) {
      if (s == null || s.isEmpty()) {
        return null;
      }

      switch (this) {
        case BOOLEAN:
          return BeeUtils.toBooleanOrNull(s);
        case INTEGER:
          return BeeUtils.toIntOrNull(s);
        case LONG:
          return BeeUtils.toLongOrNull(s);
        case DOUBLE:
          return BeeUtils.toDoubleOrNull(s);
        case DECIMAL:
          return BeeUtils.toDecimalOrNull(s);
        case DATE:
          return BeeUtils.toIntOrNull(s);
        case DATETIME:
          return BeeUtils.toLongOrNull(s);
        case CHAR:
        case STRING:
          return s;
        default:
          Assert.untouchable();
          return null;
      }
    }
  }

  /**
   * Contains a list of SQL functions used by the system.
   */

  public enum Function {
    BITAND, IF, CASE, CAST,
    PLUS, MINUS, MULTIPLY, DIVIDE, BULK,
    MIN, MAX, SUM, AVG, COUNT
  }

  /**
   * Contains a list of SQL keywords used by the system.
   */

  public enum SqlKeyword {
    DB_NAME, DB_SCHEMA, DB_TABLES, DB_FIELDS, DB_KEYS, DB_FOREIGNKEYS,
    RENAME_TABLE, DROP_TABLE, DROP_FOREIGNKEY, CREATE_INDEX,
    ADD_CONSTRAINT, PRIMARY_KEY, UNIQUE_KEY, FOREIGN_KEY,
    TEMPORARY, TEMPORARY_NAME, NOT_NULL, CASCADE, SET_NULL
  }

  public static final String TBL_NAME = "tblName";

  public static final String FLD_NAME = "fldName";
  public static final String FLD_TYPE = "fldType";
  public static final String FLD_NULL = "fldNullable";
  public static final String FLD_LENGTH = "fldLength";
  public static final String FLD_PRECISION = "fldPrecision";
  public static final String FLD_SCALE = "fldScale";

  public static final String KEY_NAME = "keyName";
  public static final String KEY_TYPE = "keyType";

  public static final String FK_REF_TABLE = "fkRefTable";

  private BeeConstants() {
  }
}
