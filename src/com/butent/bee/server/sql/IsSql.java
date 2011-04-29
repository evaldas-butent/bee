package com.butent.bee.server.sql;

import java.util.List;

public interface IsSql {

  List<Object> getSqlParams();

  String getSqlString(SqlBuilder builder, boolean paramMode);
}
