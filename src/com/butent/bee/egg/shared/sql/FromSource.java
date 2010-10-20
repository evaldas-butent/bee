package com.butent.bee.egg.shared.sql;

import java.util.List;

public interface FromSource {
  String getAlias();

  String getCondition(SqlBuilder builder, boolean queryMode);

  String getJoinMode();

  List<Object> getQueryParameters();

  Object getSource();
}
