package com.butent.bee.shared.sql;

import com.butent.bee.shared.sql.BeeConstants.DataType;
import com.butent.bee.shared.sql.BeeConstants.Keyword;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

class PostgreSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keyword option, Map<String, Object> params) {
    switch (option) {
      case DB_NAME:
        return "SELECT current_database() as " + sqlQuote("dbName");

      case DB_SCHEMA:
        return "SELECT current_schema() as " + sqlQuote("dbSchema");

      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    return "\"" + value + "\"";
  }

  @Override
  protected String sqlTransform(Object x) {
    String s = super.sqlTransform(x);

    if (x instanceof CharSequence) {
      s = s.replace("\\", "\\\\");
    }
    return s;
  }

  @Override
  protected String sqlType(DataType type, int precision, int scale) {
    switch (type) {
      case BOOLEAN:
        return "NUMERIC(1)";
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
