package com.butent.bee.egg.shared.sql;

import java.util.List;

public interface FromSource {
  public Object getSource();

  public String getAlias();

  public String getJoinMode();

  public String getCondition(boolean queryMode);

  public List<Object> getQueryParameters();
}
