package com.butent.bee.server.sql;

import com.butent.bee.shared.BeeConst.SqlEngine;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Enables to generate SQL statements without a particular SQL server configuration set.
 */

class GenericSqlBuilder extends SqlBuilder {

  @Override
  public SqlEngine getEngine() {
    return SqlEngine.GENERIC;
  }

  @Override
  protected String getAuditTrigger(String auditTable, String idName, Collection<String> fields) {
    return null;
  }

  @Override
  protected String getRelationTrigger(List<Map<String, String>> fields) {
    return null;
  }

  @Override
  protected String sqlQuote(String value) {
    return value;
  }
}
