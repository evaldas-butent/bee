package com.butent.bee.shared.sql;

import com.butent.bee.shared.data.value.Value;

import java.util.ArrayList;
import java.util.List;

class ConstantExpression extends Expression {

  private Value constant;

  public ConstantExpression(Value value) {
    this.constant = value;
  }

  protected ConstantExpression() {
    super();
  }

  @Override
  public void deserialize(String s) {
    setSafe();
    this.constant = Value.restore(s);
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> param = new ArrayList<Object>(1);
    param.add(constant.getObjectValue());
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
