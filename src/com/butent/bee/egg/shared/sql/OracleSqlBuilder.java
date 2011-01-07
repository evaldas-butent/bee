package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

class OracleSqlBuilder extends SqlBuilder {

  protected String sqlKeyword(Keywords option, Object... params) {
    switch (option) {
      case DB_SCHEMA:
        return "SELECT sys_context('USERENV', 'CURRENT_SCHEMA') as dbSchema FROM dual";

      case DB_TABLES:
        IsCondition tableWh = null;

        for (int i = 1; i < 3; i++) {
          if (!BeeUtils.isEmpty(params[i])) {
            tableWh = SqlUtils.and(tableWh,
                SqlUtils.equal("t", i == 1 ? "OWNER" : "TABLE_NAME", params[i]));
          }
        }
        return new SqlSelect()
          .addFields("t", "TABLE_NAME")
          .addFrom("ALL_TABLES", "t")
          .setWhere(tableWh)
          .getQuery(this);

      case DB_FOREIGNKEYS:
        IsCondition foreignWh = SqlUtils.equal("c", "CONSTRAINT_TYPE", "R");

        for (int i = 1; i < 3; i++) {
          if (!BeeUtils.isEmpty(params[i])) {
            foreignWh = SqlUtils.and(foreignWh,
                SqlUtils.equal("c", i == 1 ? "OWNER" : "TABLE_NAME", params[i]));
          }
        }
        if (!BeeUtils.isEmpty(params[3])) {
          foreignWh = SqlUtils.and(foreignWh, SqlUtils.equal("r", "TABLE_NAME", params[3]));
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
        return "BITAND(" + params[0] + "," + params[1] + ")";

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
}