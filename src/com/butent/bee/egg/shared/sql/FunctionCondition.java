package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class FunctionCondition implements Condition {

  private final String function;
  private final Expression expression;
  private final Expression[] values;

  public FunctionCondition(String func, Expression expr, Expression... vals) {
    Assert.notEmpty(func);
    Assert.notEmpty(expr);
    Assert.arrayLength(vals, 1);

    function = func;
    expression = expr;
    values = vals;
  }

  @Override
  public String getCondition(SqlBuilder builder, boolean paramMode) {
    StringBuilder sb = new StringBuilder();

    sb.append(function).append("(").append(
        expression.getExpression(builder, false));

    for (Expression val : values) {
      sb.append(", ").append(val.getExpression(builder, paramMode));
    }
    return sb.append(")").toString();
  }

  @Override
  public List<Object> getParameters() {
    List<Object> paramList = null;

    for (Expression e : values) {
      List<Object> eList = e.getParameters();

      if (!BeeUtils.isEmpty(eList)) {
        if (BeeUtils.isEmpty(paramList)) {
          paramList = eList;
        } else {
          paramList.addAll(eList);
        }
      }
    }
    return paramList;
  }
}
