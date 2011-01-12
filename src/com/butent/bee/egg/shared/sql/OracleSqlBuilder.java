package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map;

class OracleSqlBuilder extends SqlBuilder {

  protected String sqlKeyword(Keywords option, Map<String, Object> params) {
    switch (option) {
      case DB_SCHEMA:
        return "SELECT sys_context('USERENV', 'CURRENT_SCHEMA') as dbSchema FROM dual";

      case DB_TABLES:
        IsCondition tableWh = null;

        if (!BeeUtils.isEmpty(params.get("dbSchema"))) {
          tableWh = SqlUtils.equal("t", "OWNER", params.get("dbSchema"));
        }
        if (!BeeUtils.isEmpty(params.get("table"))) {
          tableWh = SqlUtils.and(tableWh,
              SqlUtils.equal("t", "TABLE_NAME", params.get("table")));
        }
        return new SqlSelect()
          .addFields("t", "TABLE_NAME")
          .addFrom("ALL_TABLES", "t")
          .setWhere(tableWh)
          .getQuery(this);

      case DB_FOREIGNKEYS:
        IsCondition foreignWh = SqlUtils.equal("c", "CONSTRAINT_TYPE", "R");

        if (!BeeUtils.isEmpty(params.get("dbSchema"))) {
          foreignWh = SqlUtils.and(foreignWh,
              SqlUtils.equal("c", "OWNER", params.get("dbSchema")));
        }
        if (!BeeUtils.isEmpty(params.get("table"))) {
          foreignWh = SqlUtils.and(foreignWh,
              SqlUtils.equal("c", "TABLE_NAME", params.get("table")));
        }
        if (!BeeUtils.isEmpty(params.get("refTable"))) {
          foreignWh = SqlUtils.and(foreignWh,
              SqlUtils.equal("r", "TABLE_NAME", params.get("refTable")));
        }
        return new SqlSelect()
          .addField("c", "CONSTRAINT_NAME", "Name")
          .addField("c", "TABLE_NAME", "TableName")
          .addField("r", "TABLE_NAME", "RefTableName")
          .addFrom("ALL_CONSTRAINTS", "c")
          .addFromInner("ALL_CONSTRAINTS", "r",
              SqlUtils.join("c", "R_CONSTRAINT_NAME", "r", "CONSTRAINT_NAME"))
          .setWhere(foreignWh)
          .getQuery(this);

      case TEMPORARY:
        return "";

      case BITAND:
        return "BITAND(" + params.get("expression") + "," + params.get("value") + ")";

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
      case INTEGER:
        return "NUMERIC(10)";
      case LONG:
        return "NUMERIC(19)";
      case FLOAT:
        return "BINARY_FLOAT";
      case DOUBLE:
        return "BINARY_DOUBLE";
      case STRING:
        return "NVARCHAR2(" + precision + ")";
      default:
        return super.sqlType(type, precision, scale);
    }
  }

  @Override
  String getQuery(SqlSelect ss, boolean paramMode) {
    int limit = ss.getLimit();
    int offset = ss.getOffset();
    String sql = super.getQuery(ss, paramMode);

    if (BeeUtils.allEmpty(limit, offset)) {
      return sql;
    }
    String idAlias = sqlQuote(SqlUtils.uniqueName());
    String queryAlias = sqlQuote(SqlUtils.uniqueName());

    sql = BeeUtils.concatNoTrim(0
        , "ROWNUM AS ", idAlias, ", ", queryAlias, ".*"
        , " FROM (", sql, ") ", queryAlias);

    if (BeeUtils.isPositive(limit)) {
      sql = BeeUtils.concatNoTrim(0
          , "/*+ FIRST_ROWS(", offset + limit, ") */ ", sql
          , " WHERE ROWNUM<=", offset + limit);
    }
    sql = "SELECT " + sql;

    if (BeeUtils.isPositive(offset)) {
      queryAlias = sqlQuote(SqlUtils.uniqueName());

      sql = BeeUtils.concatNoTrim(0
          , "SELECT ", queryAlias, ".* FROM (", sql, ") ", queryAlias
          , " WHERE ", queryAlias, ".", idAlias, ">", offset);
    }
    return sql;
  }
}