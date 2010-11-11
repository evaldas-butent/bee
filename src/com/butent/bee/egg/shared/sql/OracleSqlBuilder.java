package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;

class OracleSqlBuilder extends SqlBuilder {

  @Override
  public String getTables() {
    return "select table_name from user_tables";
  }

  @Override
  protected String sqlQuote(String value) {
    return "\"" + value + "\"";
  }

  @Override
  protected Object sqlType(DataTypes type, int precission, int scale) {
    switch (type) {
      case BOOLEAN:
        return "NUMERIC(1)";
      case INTEGER:
        return "NUMERIC(10)";
      case LONG:
        return "NUMERIC(19)";
      case DOUBLE:
        return "BINARY_DOUBLE";
      case STRING:
        return "VARCHAR2(" + precission + ")";
      default:
        return super.sqlType(type, precission, scale);
    }
  }
}
