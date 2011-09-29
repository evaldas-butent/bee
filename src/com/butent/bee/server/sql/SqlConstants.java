package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains constants lists, such as keywords, data types and empty values for each of them.
 */

public final class SqlConstants {

  /**
   * Contains a list of supported data types.
   */

  public enum SqlDataType {
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

  public enum SqlFunction {
    BITAND, IF, CASE, CAST,
    PLUS, MINUS, MULTIPLY, DIVIDE, BULK,
    MIN, MAX, SUM, AVG, COUNT
  }

  /**
   * Contains a list of SQL keywords used by the system.
   */

  public enum SqlKeyword {
    DB_NAME, DB_SCHEMA, DB_TABLES, DB_FIELDS, DB_KEYS, DB_FOREIGNKEYS, DB_TRIGGERS,
    RENAME_TABLE, DROP_TABLE, DROP_FOREIGNKEY, CREATE_INDEX,
    CREATE_TRIGGER_FUNCTION, CREATE_TRIGGER,
    ADD_CONSTRAINT, PRIMARY_KEY, UNIQUE_KEY, FOREIGN_KEY,
    TEMPORARY, TEMPORARY_NAME
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

  public static final String TRIGGER_NAME = "triggerName";

  private SqlConstants() {
  }
}
