package com.butent.bee.egg.shared.sql;

public interface IsFrom extends IsSql {
  String getAlias();

  String getJoinMode();

  Object getSource();
}
