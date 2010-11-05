package com.butent.bee.egg.shared.sql;

class OracleSqlBuilder extends SqlBuilder {

  @Override
  public String getTables() {
    return "select table_name from user_tables";
  }

  @Override
  protected String sqlQuote(String value) {
    return value;
  }

}
