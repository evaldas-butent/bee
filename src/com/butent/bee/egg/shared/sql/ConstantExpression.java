package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;

import java.util.ArrayList;
import java.util.List;

class ConstantExpression implements IsExpression {

  private final Object value;

  public ConstantExpression(Object value) {
    Assert.notEmpty(value);

    this.value = value;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> param = new ArrayList<Object>(1);
    param.add(value);
    return param;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    return paramMode ? "?" : builder.sqlTransform(value);
  }
}
