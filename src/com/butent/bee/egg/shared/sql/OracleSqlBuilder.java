package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

class OracleSqlBuilder extends SqlBuilder {

  protected String sqlKeyword(Keywords option, Object... params) {
    switch (option) {
      case DB_TABLES:
        IsCondition tableWh = null;

        if (!BeeUtils.isEmpty(params[0])) {
          tableWh = SqlUtils.equal("t", "TABLE_NAME", params[0]);
        }
        return new SqlSelect()
          .addFields("t", "TABLE_NAME")
          .addFrom("USER_TABLES", "t")
          .setWhere(tableWh)
          .getQuery(this);

      case DB_FOREIGNKEYS:
        IsCondition foreignWh = SqlUtils.equal("c", "CONSTRAINT_TYPE", "R");

        if (!BeeUtils.isEmpty(params[0])) {
          foreignWh = SqlUtils.and(foreignWh, SqlUtils.equal("c", "TABLE_NAME", params[0]));
        }
        return new SqlSelect()
          .addFields("c", "CONSTRAINT_NAME")
          .addFrom("USER_CONSTRAINTS", "c")
          .setWhere(foreignWh)
          .getQuery(this);

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