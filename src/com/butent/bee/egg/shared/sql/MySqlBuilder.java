package com.butent.bee.egg.shared.sql;

class MySqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    return "`" + value + "`";
  }

  @Override
  String getCreate(SqlCreate sc, boolean paramMode) {
    return super.getCreate(sc, paramMode) + " ENGINE=InnoDB";
  }
}
