package com.butent.bee.shared.sql;

import com.butent.bee.shared.sql.BeeConstants.DataType;
import com.butent.bee.shared.sql.BeeConstants.Keyword;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

class OracleSqlBuilder extends SqlBuilder {

  protected String sqlKeyword(Keyword option, Map<String, Object> params) {
    switch (option) {
      case DB_SCHEMA:
        return "SELECT sys_context('USERENV', 'CURRENT_SCHEMA') as dbSchema FROM dual";

      case DB_TABLES:
        IsCondition wh = null;

        Object prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.equal("t", "OWNER", prm);
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("t", "TABLE_NAME", prm));
        }
        return new SqlSelect()
            .addFields("t", "TABLE_NAME")
            .addFrom("ALL_TABLES", "t")
            .setWhere(wh)
            .getQuery(this);

      case DB_FOREIGNKEYS:
        wh = SqlUtils.equal("c", "CONSTRAINT_TYPE", "R");

        prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh,
              SqlUtils.equal("c", "OWNER", prm),
              SqlUtils.equal("r", "OWNER", prm));
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("c", "TABLE_NAME", prm));
        }
        prm = params.get("refTable");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("r", "TABLE_NAME", prm));
        }
        return new SqlSelect()
            .addField("c", "CONSTRAINT_NAME", BeeConstants.FK_NAME)
            .addField("c", "TABLE_NAME", BeeConstants.FK_TABLE)
            .addField("r", "TABLE_NAME", BeeConstants.FK_REF_TABLE)
            .addFrom("ALL_CONSTRAINTS", "c")
            .addFromInner("ALL_CONSTRAINTS", "r",
                SqlUtils.join("c", "R_CONSTRAINT_NAME", "r", "CONSTRAINT_NAME"))
            .setWhere(wh)
            .getQuery(this);

      case TEMPORARY:
        return "";

      case BITAND:
        return "BITAND(" + params.get("expression") + ", " + params.get("value") + ")";

      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    return "\"" + value + "\"";
  }

  @Override
  protected Object sqlType(DataType type, int precision, int scale) {
    switch (type) {
      case BOOLEAN:
        return "NUMERIC(1)";
      case INTEGER:
      case DATE:
        return "NUMERIC(10)";
      case LONG:
      case DATETIME:
        return "NUMERIC(19)";
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

    sql = BeeUtils.concat(1,
        "ROWNUM AS", idAlias + ",", queryAlias + ".*",
        "FROM", BeeUtils.parenthesize(sql), queryAlias);

    if (BeeUtils.isPositive(limit)) {
      sql = BeeUtils.concat(1,
          "/*+ FIRST_ROWS" + BeeUtils.parenthesize(offset + limit), "*/", sql,
          "WHERE ROWNUM <=", offset + limit);
    }
    sql = "SELECT " + sql;

    if (BeeUtils.isPositive(offset)) {
      queryAlias = sqlQuote(SqlUtils.uniqueName());

      sql = BeeUtils.concat(1,
          "SELECT", queryAlias + ".*",
          "FROM", BeeUtils.parenthesize(sql), queryAlias,
          "WHERE", queryAlias + "." + idAlias, ">", offset);
    }
    return sql;
  }
}