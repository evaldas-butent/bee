package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map;

class MsSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keywords option, Map<String, Object> params) {
    switch (option) {
      case DB_NAME:
        return "SELECT db_name() as dbName";

      case DB_SCHEMA:
        return "SELECT schema_name() as dbSchema";

      case TEMPORARY:
        return "";

      case TEMPORARY_NAME:
        return "#" + params.get("name");

      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    return "[" + value + "]";
  }

  @Override
  protected Object sqlType(DataTypes type, int precision, int scale) {
    switch (type) {
      case FLOAT:
        return "REAL";
      case DOUBLE:
        return "FLOAT";
      default:
        return super.sqlType(type, precision, scale);
    }
  }

  @Override
  String getCreate(SqlCreate sc, boolean paramMode) {
    if (BeeUtils.isEmpty(sc.getSource())) {
      return super.getCreate(sc, paramMode);
    }
    Assert.notNull(sc);
    Assert.state(!sc.isEmpty());

    return sc.getSource().getSqlString(this, paramMode).replace(" FROM ",
        " INTO " + sc.getTarget().getSqlString(this, paramMode) + " FROM ");
  }

  @Override
  String getQuery(SqlSelect ss, boolean paramMode) {
    int limit = ss.getLimit();
    int offset = ss.getOffset();
    String sql = super.getQuery(ss, paramMode);

    if (BeeUtils.allEmpty(limit, offset)) {
      return sql;
    }
    String select = "SELECT " + (ss.isDistinctMode() ? "DISTINCT " : "");
    sql = sql.substring(select.length());
    String top = "";
    String numbering = "";
    String idAlias = "";
    boolean hasUnion = !BeeUtils.isEmpty(ss.getUnion());

    if (BeeUtils.isPositive(limit)) {
      top = BeeUtils.concatNoTrim(0, "TOP ", (offset + limit) + " ");
    }
    if (BeeUtils.isPositive(offset)) {
      String order = "ORDER BY (SELECT 0)";

      if (!BeeUtils.isEmpty(ss.getOrderBy())) {
        int idx = sql.lastIndexOf(" ORDER BY ");
        order = sql.substring(idx + 1);
        sql = sql.substring(0, idx);
      }
      idAlias = sqlQuote(SqlUtils.uniqueName());
      numbering = BeeUtils.concatNoTrim(0, "ROW_NUMBER() OVER (", order, ") AS ", idAlias, ", ");
    }
    if (hasUnion) {
      String queryAlias = sqlQuote(SqlUtils.uniqueName());

      sql = BeeUtils.concatNoTrim(0,
          "SELECT ", top, numbering, queryAlias, ".* FROM (", select, sql, ") ", queryAlias);
    } else {
      sql = BeeUtils.concatNoTrim(0, select, top, numbering, sql);
    }
    if (!BeeUtils.isEmpty(idAlias)) {
      String queryAlias = sqlQuote(SqlUtils.uniqueName());

      sql = BeeUtils.concatNoTrim(0
          , "SELECT ", queryAlias, ".* FROM (", sql, ") ", queryAlias
          , " WHERE ", queryAlias, ".", idAlias, ">", offset);
    }
    return sql;
  }
}
