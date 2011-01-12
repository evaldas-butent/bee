package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map;

class MySqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keywords option, Map<String, Object> params) {
    switch (option) {
      case DB_SCHEMA:
        return "SELECT schema() as dbSchema";

      case DROP_FOREIGNKEY:
        StringBuilder drop = new StringBuilder("ALTER TABLE ")
          .append(params.get("table"))
          .append(" DROP FOREIGN KEY ")
          .append(params.get("name"));
        return drop.toString();

      case DB_TABLES:
        String sql = "SHOW TABLES";

        if (!BeeUtils.isEmpty(params.get("dbSchema"))) {
          sql += " IN " + params.get("dbSchema");
        }
        if (!BeeUtils.isEmpty(params.get("table"))) {
          sql += " LIKE " + sqlTransform(params.get("table"));
        }
        return sql;

      case DB_FOREIGNKEYS:
        IsCondition foreignWh = null;

        if (!BeeUtils.isEmpty(params.get("dbName"))) {
          foreignWh = SqlUtils.equal("t", "table_catalog", params.get("dbName"));
        }
        if (!BeeUtils.isEmpty(params.get("dbSchema"))) {
          foreignWh = SqlUtils.and(foreignWh,
              SqlUtils.equal("t", "table_schema", params.get("dbSchema")));
        }
        if (!BeeUtils.isEmpty(params.get("table"))) {
          foreignWh = SqlUtils.and(foreignWh,
              SqlUtils.equal("t", "table_name", params.get("table")));
        }
        if (!BeeUtils.isEmpty(params.get("refTable"))) {
          foreignWh = SqlUtils.and(foreignWh,
              SqlUtils.equal("c", "referenced_table_name", params.get("refTable")));
        }
        return new SqlSelect()
          .addField("c", "constraint_name", "Name")
          .addField("t", "table_name", "TblName")
          .addField("c", "referenced_table_name", "RefTblName")
          .addFrom("information_schema.referential_constraints", "c")
          .addFromInner("information_schema.table_constraints", "t",
              SqlUtils.joinUsing("c", "t", "constraint_name"))
          .setWhere(foreignWh)
          .getQuery(this);

      default:
        return super.sqlKeyword(option, params);
    }
  }

  @Override
  protected String sqlQuote(String value) {
    return "`" + value + "`";
  }

  @Override
  String getCreate(SqlCreate sc, boolean paramMode) {
    String sql = super.getCreate(sc, paramMode);

    if (BeeUtils.isEmpty(sc.getSource())) {
      sql += " ENGINE=InnoDB";
    }
    return sql;
  }

  @Override
  String getQuery(SqlSelect ss, boolean paramMode) {
    int limit = ss.getLimit();
    int offset = ss.getOffset();
    String sql = super.getQuery(ss, paramMode);

    if (BeeUtils.isPositive(limit)) {
      sql += " LIMIT " + limit;
    } else if (BeeUtils.isPositive(offset)) {
      sql += " LIMIT " + (int) 1e9; // TODO Dummy MySql
    }
    if (BeeUtils.isPositive(offset)) {
      sql += " OFFSET " + offset;
    }
    return sql;
  }
}
