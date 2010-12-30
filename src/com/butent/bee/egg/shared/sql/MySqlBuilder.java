package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

class MySqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keywords option, Object... params) {
    switch (option) {
      case DB_SCHEMA:
        return "SELECT schema() as dbSchema";

      case DROP_FOREIGNKEY:
        StringBuilder drop = new StringBuilder("ALTER TABLE ")
          .append(params[0])
          .append(" DROP FOREIGN KEY ")
          .append(params[1]);
        return drop.toString();

      case DB_TABLES:
        String sql = "SHOW TABLES";

        if (!BeeUtils.isEmpty(params[1])) {
          sql += " IN " + params[1];
        }
        if (!BeeUtils.isEmpty(params[2])) {
          sql += " LIKE '" + params[2] + "'";
        }
        return sql;

      case DB_FOREIGNKEYS:
        IsCondition foreignWh = null;

        for (int i = 0; i < 3; i++) {
          if (!BeeUtils.isEmpty(params[i])) {
            foreignWh = SqlUtils.and(foreignWh, SqlUtils.equal("t",
                i == 0 ? "table_catalog" : (i == 1 ? "table_schema" : "table_name"), params[i]));
          }
        }
        if (!BeeUtils.isEmpty(params[3])) {
          foreignWh = SqlUtils.and(foreignWh,
              SqlUtils.equal("c", "referenced_table_name", params[3]));
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
}
