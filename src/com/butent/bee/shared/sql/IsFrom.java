package com.butent.bee.shared.sql;

public interface IsFrom extends IsSql, HasSource {

  String getAlias();

  String getJoinMode();

  Object getSource();
}
