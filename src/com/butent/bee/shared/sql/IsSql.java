package com.butent.bee.shared.sql;

import java.util.List;

public interface IsSql {

  List<Object> getSqlParams();

  String getSqlString(SqlBuilder builder, boolean paramMode);
}
