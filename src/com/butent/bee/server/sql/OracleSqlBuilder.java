package com.butent.bee.server.sql;

import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlConstants.SqlFunction;
import com.butent.bee.server.sql.SqlConstants.SqlKeyword;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

/**
 * Contains specific requirements for SQL statement building for Oracle SQL server.
 */

class OracleSqlBuilder extends SqlBuilder {

  @Override
  protected String getSelect(SqlSelect ss) {
    int limit = ss.getLimit();
    int offset = ss.getOffset();
    String sql = super.getSelect(ss);

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

  @Override
  protected String sqlFunction(SqlFunction function, Map<String, Object> params) {
    switch (function) {
      case BITAND:
        return "BITAND(" + params.get("expression") + ", " + params.get("value") + ")";

      default:
        return super.sqlFunction(function, params);
    }
  }

  @Override
  protected String sqlKeyword(SqlKeyword option, Map<String, Object> params) {
    switch (option) {
      case DB_SCHEMA:
        return "SELECT sys_context('USERENV', 'CURRENT_SCHEMA') AS " + sqlQuote("dbSchema")
            + " FROM dual";

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
            .getSqlString(this);

      case DB_FIELDS:
        wh = null;

        prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.equal("c", "OWNER", prm);
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("c", "TABLE_NAME", prm));
        }
        return new SqlSelect()
            .addField("c", "TABLE_NAME", SqlConstants.TBL_NAME)
            .addField("c", "COLUMN_NAME", SqlConstants.FLD_NAME)
            .addField("c", "NULLABLE", SqlConstants.FLD_NULL)
            .addField("c", "DATA_TYPE", SqlConstants.FLD_TYPE)
            .addField("c", "DATA_LENGTH", SqlConstants.FLD_LENGTH)
            .addField("c", "DATA_PRECISION", SqlConstants.FLD_PRECISION)
            .addField("c", "DATA_SCALE", SqlConstants.FLD_SCALE)
            .addFrom("ALL_TAB_COLUMNS", "c")
            .setWhere(wh)
            .addOrder("c", "COLUMN_ID")
            .getSqlString(this);

      case DB_KEYS:
        wh = null;

        prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.equal("k", "OWNER", prm);
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("k", "TABLE_NAME", prm));
        }
        prm = params.get("keyTypes");
        if (!BeeUtils.isEmpty(prm)) {
          IsCondition typeWh = null;

          for (SqlKeyword type : (SqlKeyword[]) prm) {
            String tp;

            switch (type) {
              case PRIMARY_KEY:
                tp = "P";
                break;

              case UNIQUE_KEY:
                tp = "U";
                break;

              case FOREIGN_KEY:
                tp = "R";
                break;

              default:
                tp = null;
            }
            if (!BeeUtils.isEmpty(tp)) {
              typeWh = SqlUtils.or(typeWh, SqlUtils.equal("k", "CONSTRAINT_TYPE", tp));
            }
          }
          if (!BeeUtils.isEmpty(typeWh)) {
            wh = SqlUtils.and(wh, typeWh);
          }
        }
        return new SqlSelect()
            .addField("k", "TABLE_NAME", SqlConstants.TBL_NAME)
            .addField("k", "CONSTRAINT_NAME", SqlConstants.KEY_NAME)
            .addField("k", "CONSTRAINT_TYPE", SqlConstants.KEY_TYPE)
            .addFrom("ALL_CONSTRAINTS", "k")
            .setWhere(wh)
            .getSqlString(this);

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
            .addField("c", "CONSTRAINT_NAME", SqlConstants.KEY_NAME)
            .addField("c", "TABLE_NAME", SqlConstants.TBL_NAME)
            .addField("r", "TABLE_NAME", SqlConstants.FK_REF_TABLE)
            .addFrom("ALL_CONSTRAINTS", "c")
            .addFromInner("ALL_CONSTRAINTS", "r",
                SqlUtils.join("c", "R_CONSTRAINT_NAME", "r", "CONSTRAINT_NAME"))
            .setWhere(wh)
            .getSqlString(this);

      case TEMPORARY:
        return "";

      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    return "\"" + value + "\"";
  }

  @Override
  protected String sqlType(SqlDataType type, int precision, int scale) {
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
}