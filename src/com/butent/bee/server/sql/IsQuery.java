package com.butent.bee.server.sql;

import java.util.Map;

public interface IsQuery extends IsSql, HasSource {

  boolean getParamMode();

  String getQuery();

  String getQuery(SqlBuilder builder);

  Map<Integer, Object> getQueryParams();

  boolean isEmpty();

  void setParamMode(boolean mode);
}
