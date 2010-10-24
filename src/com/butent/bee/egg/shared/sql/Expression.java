package com.butent.bee.egg.shared.sql;

import java.util.List;

public interface Expression {
  String getExpression(SqlBuilder builder, boolean paramMode);

  List<Object> getParameters();
}
