package com.butent.bee.egg.shared.sql;

class MsSqlBuilder extends SqlBuilder {

  @Override
  public String getTables() {
    return "select table_name from information_schema.tables";
  }

  @Override
  protected String sqlQuote(String value) {
    return "[" + value + "]";
  }
}
