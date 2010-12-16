package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

class MsSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlKeyword(Keywords option, Object... params) {
    switch (option) {
      case DB_NAME:
        return "SELECT db_name() as dbName";

      case DB_SCHEMA:
        return "SELECT schema_name() as dbSchema";

      case TEMPORARY:
        return "";

      case TEMPORARY_NAME:
        return "#" + params[0];

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
}
