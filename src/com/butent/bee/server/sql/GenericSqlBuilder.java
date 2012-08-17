package com.butent.bee.server.sql;

import com.butent.bee.shared.BeeConst.SqlEngine;

/**
 * Enables to generate SQL statements without a particular SQL server configuration set.
 */

class GenericSqlBuilder extends SqlBuilder {

  @Override
  public SqlEngine getEngine() {
    return SqlEngine.GENERIC;
  }

  @Override
  protected String sqlQuote(String value) {
    return value;
  }
}
