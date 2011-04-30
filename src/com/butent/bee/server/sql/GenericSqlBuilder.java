package com.butent.bee.server.sql;

/**
 * Enables to generate SQL statements without a particular SQL server configuration set.
 */

class GenericSqlBuilder extends SqlBuilder {

  @Override
  protected String sqlQuote(String value) {
    return value;
  }
}
