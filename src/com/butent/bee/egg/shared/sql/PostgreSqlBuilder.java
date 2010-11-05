package com.butent.bee.egg.shared.sql;

class PostgreSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    String quote = "\"";
    return quote + value + quote;
  }
}
