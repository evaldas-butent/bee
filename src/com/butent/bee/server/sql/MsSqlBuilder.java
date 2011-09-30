package com.butent.bee.server.sql;

import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlConstants.SqlKeyword;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

/**
 * Contains specific requirements for SQL statement building for Microsoft SQL server.
 */

class MsSqlBuilder extends SqlBuilder {

  @Override
  protected String getCreate(SqlCreate sc) {
    if (BeeUtils.isEmpty(sc.getDataSource())) {
      return super.getCreate(sc);
    }
    Assert.notNull(sc);
    Assert.state(!sc.isEmpty());

    return sc.getDataSource().getSqlString(this).replace(" FROM ",
        " INTO " + sc.getTarget().getSqlString(this) + " FROM ");
  }

  @Override
  protected String getSelect(SqlSelect ss) {
    int limit = ss.getLimit();
    int offset = ss.getOffset();
    String sql = super.getSelect(ss);

    if (BeeUtils.allEmpty(limit, offset)) {
      return sql;
    }
    String top = "";
    String numbering = "";
    String idAlias = "";
    boolean hasUnion = !BeeUtils.isEmpty(ss.getUnion());

    if (BeeUtils.isPositive(limit)) {
      top = BeeUtils.concat(1, "TOP", offset + limit);
    }
    if (BeeUtils.isPositive(offset)) {
      String order = "ORDER BY (SELECT 0)";

      if (!BeeUtils.isEmpty(ss.getOrderBy())) {
        int idx = sql.lastIndexOf(" ORDER BY ");
        order = sql.substring(idx + 1);
        sql = sql.substring(0, idx);
      }
      idAlias = sqlQuote(SqlUtils.uniqueName());
      numbering = BeeUtils.concat(1,
          "ROW_NUMBER() OVER", BeeUtils.parenthesize(order), "AS", idAlias + ",");
    }
    if (hasUnion) {
      String queryAlias = sqlQuote(SqlUtils.uniqueName());

      sql = BeeUtils.concat(1,
          "SELECT", top, numbering, queryAlias + ".*",
          "FROM", BeeUtils.parenthesize(sql), queryAlias);
    } else {
      String select = "SELECT " + (ss.isDistinctMode() ? "DISTINCT " : "");
      sql = BeeUtils.concat(1,
          select, top, numbering, sql.substring(select.length()));
    }
    if (!BeeUtils.isEmpty(idAlias)) {
      String queryAlias = sqlQuote(SqlUtils.uniqueName());

      sql = BeeUtils.concat(1,
          "SELECT", queryAlias + ".*",
          "FROM", BeeUtils.parenthesize(sql), queryAlias,
          "WHERE", queryAlias + "." + idAlias, ">", offset);
    }
    return sql;
  }

  @Override
  protected String sqlKeyword(SqlKeyword option, Map<String, Object> params) {
    switch (option) {
      case CREATE_TRIGGER:
        List<String[]> content = (List<String[]>) params.get("content");
        String text = "SET NOCOUNT ON;";

        for (String[] entry : content) {
          String fldName = entry[0];
          String relTable = entry[1];
          String relField = entry[2];

          text = BeeUtils.concat(1, text,
              new SqlDelete(relTable).addFrom("deleted")
                  .setWhere(SqlUtils.join(relTable, relField, "deleted", fldName))
                  .getQuery(),
              ";");
        }
        return BeeUtils.concat(1,
            "CREATE TRIGGER", params.get("name"),
            "ON", params.get("table"), params.get("timing"), params.get("event"),
            "AS BEGIN", text, "END;");

      case DB_NAME:
        return "SELECT db_name() AS " + sqlQuote("dbName");

      case DB_SCHEMA:
        return "SELECT schema_name() AS " + sqlQuote("dbSchema");

      case DB_TRIGGERS:
        IsCondition wh = null;

        Object prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("o", "name", prm));
        }
        return new SqlSelect()
            .addField("o", "name", SqlConstants.TBL_NAME)
            .addField("t", "name", SqlConstants.TRIGGER_NAME)
            .addFrom("sys.triggers", "t")
            .addFromInner("sys.objects", "o", SqlUtils.join("t", "parent_id", "o", "object_id"))
            .setWhere(wh)
            .getSqlString(this);

      case TEMPORARY:
        return "";

      case TEMPORARY_NAME:
        return "#" + params.get("name");

      case RENAME_TABLE:
        return BeeUtils.concat(1,
            "sp_rename", params.get("nameFrom"), ",", params.get("nameTo"));

      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    return "[" + value + "]";
  }

  @Override
  protected String sqlType(SqlDataType type, int precision, int scale) {
    switch (type) {
      case DOUBLE:
        return "FLOAT";
      case TEXT:
        return "VARCHAR(MAX)";
      default:
        return super.sqlType(type, precision, scale);
    }
  }
}
