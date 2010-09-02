package com.butent.bee.egg.shared.sql;

import java.util.List;

public interface Condition {
  public String getCondition(boolean queryMode);

  public List<Object> getQueryParameters();
}
