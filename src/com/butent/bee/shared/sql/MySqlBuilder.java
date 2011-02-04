package com.butent.bee.shared.sql;

import com.butent.bee.shared.sql.BeeConstants.Keywords;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

class MySqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keywords option, Map<String, Object> params) {
    switch (option) {
      case DB_SCHEMA:
        return "SELECT schema() as dbSchema";

      case DROP_FOREIGNKEY:
        return BeeUtils.concat(1,
            "ALTER TABLE", params.get("table"),
            "DROP FOREIGN KEY", params.get("name"));

      case DB_TABLES:
        String sql = BeeUtils.concat(" IN ", "SHOW TABLES", params.get("dbSchema"));
        sql = BeeUtils.concat(" LIKE ", sql, sqlTransform(params.get("table")));
        return sql;

      case DB_FOREIGNKEYS:
        IsCondition wh = null;

        Object prm = params.get("dbName");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh,
              SqlUtils.equal("c", "constraint_catalog", prm),
              SqlUtils.equal("t", "table_catalog", prm));
        }
        prm = params.get("dbSchema");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh,
              SqlUtils.equal("c", "constraint_schema", prm),
              SqlUtils.equal("t", "table_schema", prm));
        }
        prm = params.get("table");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("t", "table_name", prm));
        }
        prm = params.get("refTable");
        if (!BeeUtils.isEmpty(prm)) {
          wh = SqlUtils.and(wh, SqlUtils.equal("c", "referenced_table_name", prm));
        }
        return new SqlSelect()
          .addField("c", "constraint_name", "Name")
          .addField("t", "table_name", "TblName")
          .addField("c", "referenced_table_name", "RefTblName")
          .addFrom("information_schema.referential_constraints", "c")
          .addFromInner("information_schema.table_constraints", "t",
              SqlUtils.joinUsing("c", "t", "constraint_name"))
          .setWhere(wh)
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

    if (BeeUtils.isEmpty(sc.getDataSource())) {
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
      sql += " LIMIT " + (int) 1e9;
    }
    if (BeeUtils.isPositive(offset)) {
      sql += " OFFSET " + offset;
    }
    return sql;
  }
}
