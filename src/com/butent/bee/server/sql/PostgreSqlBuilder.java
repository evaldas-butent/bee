package com.butent.bee.server.sql;

import com.butent.bee.server.sql.BeeConstants.DataType;
import com.butent.bee.server.sql.BeeConstants.Keyword;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

/**
 * Contains specific requirements for SQL statement building for PostgreSQL server.
 */

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
    Object val;

    if (x instanceof Value) {
      val = ((Value) x).getObjectValue();
    } else {
      val = x;
    }
    String s = super.sqlTransform(val);

    if (val instanceof CharSequence) {
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
