package com.butent.bee.egg.shared.sql;

import java.util.List;

public interface FromSource {
  String getAlias();

  String getFrom(SqlBuilder builder, boolean paramMode);

  String getJoinMode();

  List<Object> getParameters();

  Object getSource();
}
