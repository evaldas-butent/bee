package com.butent.bee.server.sql;

import java.util.List;

/**
 * Ensures that all implementing classes of this interface would have a SQL 
 * string.
 */

public interface IsSql {

  List<Object> getSqlParams();

  String getSqlString(SqlBuilder builder, boolean paramMode);
}
