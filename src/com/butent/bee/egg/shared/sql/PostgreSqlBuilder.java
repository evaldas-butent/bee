package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;

class PostgreSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keywords option, Object... params) {
    switch (option) {
      case DB_NAME:
        return "SELECT current_database() as dbName";

      case DB_SCHEMA:
        return "SELECT current_schema() as dbSchema";

      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    String quote = "\"";
    return quote + value + quote;
  }

  @Override
  protected Object sqlType(DataTypes type, int precision, int scale) {
    switch (type) {
      case BOOLEAN:
        return "NUMERIC(1)";
      case FLOAT:
        return "REAL";
      case DOUBLE:
        return "DOUBLE PRECISION";
      default:
        return super.sqlType(type, precision, scale);
    }
  }
}
