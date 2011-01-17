package com.butent.bee.shared.sql;

import java.util.Map;

public interface IsQuery extends IsSql {

  boolean getParamMode();

  String getQuery();

  String getQuery(SqlBuilder builder);

  Map<Integer, Object> getQueryParams();

  boolean isEmpty();

  void setParamMode(boolean mode);
}
