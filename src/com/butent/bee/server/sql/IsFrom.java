package com.butent.bee.server.sql;

public interface IsFrom extends IsSql, HasSource {

  String getAlias();

  Object getSource();
}
