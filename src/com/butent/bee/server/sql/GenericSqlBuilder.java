package com.butent.bee.server.sql;

class GenericSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    return value;
  }
}
