package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

class ConstantExpression implements IsExpression {

  private final Object constant;

  public ConstantExpression(Object value) {
    Assert.notNull(value);
    this.constant = value;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> param = new ArrayList<Object>(1);
    param.add(constant);
    return param;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    return paramMode ? "?" : builder.sqlTransform(constant);
  }

  @Override
  public String getValue() {
    return BeeUtils.transformNoTrim(constant);
  }
}
