package com.butent.bee.server.sql;

import com.butent.bee.shared.BeeConst.SqlEngine;
import com.butent.bee.shared.utils.BeeUtils;

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
  protected String getSelect(SqlSelect ss) {
    int limit = ss.getLimit();
    int offset = ss.getOffset();
    String sql = super.getSelect(ss);

    if (BeeUtils.isPositive(limit)) {
      sql += " LIMIT " + limit;
    }
    if (BeeUtils.isPositive(offset)) {
      sql += " OFFSET " + offset;
    }
    return sql;
  }

  @Override
  protected String sqlQuote(String value) {
    return value;
  }
}
