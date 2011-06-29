package com.butent.bee.server.sql;

/**
 * Ensures that all implementing classes of this interface would have a SQL string.
 */

public interface IsSql {

  String getSqlString(SqlBuilder builder);
}
