package com.butent.bee.egg.shared.sql;

import java.util.List;

public interface IsSql {
  List<Object> getSqlParams();

  String getSqlString(SqlBuilder builder, boolean paramMode);
}
