package com.butent.bee.shared.sql;

import com.google.common.collect.Lists;

import com.butent.bee.shared.data.value.Value;

import java.util.List;

class ConstantExpression implements IsExpression {

  private final Value constant;

  public ConstantExpression(Value value) {
    this.constant = value;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> param = Lists.newArrayList();

    if (constant != null) {
      param.add(constant.getObjectValue());
    }
    return param;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    return paramMode ? "?" : builder.sqlTransform(constant);
  }

  @Override
  public Value getValue() {
    return constant;
  }
}
