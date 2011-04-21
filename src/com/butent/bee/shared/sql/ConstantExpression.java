package com.butent.bee.shared.sql;

import com.butent.bee.shared.data.value.Value;

import java.util.ArrayList;
import java.util.List;

class ConstantExpression implements IsExpression {

  private final Value constant;

  public ConstantExpression(Value value) {
    this.constant = value;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> param = new ArrayList<Object>(1);

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
