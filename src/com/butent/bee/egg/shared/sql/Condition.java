package com.butent.bee.egg.shared.sql;

import java.util.List;

public interface Condition {
  String getCondition(SqlBuilder builder, boolean queryMode);

  List<Object> getQueryParameters();
}
