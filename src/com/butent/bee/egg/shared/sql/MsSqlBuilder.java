package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;

class MsSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    return "[" + value + "]";
  }

  @Override
  protected Object sqlType(DataTypes type, int precission, int scale) {
    switch (type) {
      case DOUBLE:
        return "FLOAT";
      default:
        return super.sqlType(type, precission, scale);
    }
  }
}
