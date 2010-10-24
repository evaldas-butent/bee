package com.butent.bee.egg.shared.sql;

class OracleSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    return value;
  }

}
