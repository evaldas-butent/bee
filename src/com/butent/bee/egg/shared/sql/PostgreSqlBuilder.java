package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map;

class PostgreSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keywords option, Map<String, Object> params) {
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
    return "\"" + value + "\"";
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

  @Override
  String getQuery(SqlSelect ss, boolean paramMode) {
    int limit = ss.getLimit();
    int offset = ss.getOffset();
    String sql = super.getQuery(ss, paramMode);

    if (BeeUtils.isPositive(limit)) {
      sql += " LIMIT " + limit;
    }
    if (BeeUtils.isPositive(offset)) {
      sql += " OFFSET " + offset;
    }
    return sql;
  }
}
