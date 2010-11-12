package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;

class PostgreSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    String quote = "\"";
    return quote + value + quote;
  }

  @Override
  protected Object sqlType(DataTypes type, int precission, int scale) {
    switch (type) {
      case BOOLEAN:
        return "TINYINT";
      case DOUBLE:
        return "DOUBLE PRECISION";
      default:
        return super.sqlType(type, precission, scale);
    }
  }
}
