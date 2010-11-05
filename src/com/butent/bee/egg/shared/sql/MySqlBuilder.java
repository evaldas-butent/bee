package com.butent.bee.egg.shared.sql;

class MySqlBuilder extends SqlBuilder {

  @Override
  public String getTables() {
    return "show tables";
  }

  @Override
  protected String sqlQuote(String value) {
    return "`" + value + "`";
  }
}
