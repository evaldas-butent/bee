package com.butent.bee.egg.shared.sql;

class MySqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    return "`" + value + "`";
  }
}
