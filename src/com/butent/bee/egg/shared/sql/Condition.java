package com.butent.bee.egg.shared.sql;

import java.util.List;

public interface Condition {
  String getCondition(boolean queryMode);

  List<Object> getQueryParameters();
}
