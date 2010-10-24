package com.butent.bee.egg.shared.sql;

class GenericSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    return value;
  }
}
