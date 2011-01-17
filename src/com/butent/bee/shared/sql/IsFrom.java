package com.butent.bee.shared.sql;

public interface IsFrom extends IsSql {

  String getAlias();

  String getJoinMode();

  Object getSource();
}
