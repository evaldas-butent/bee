package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class FunctionCondition implements IsCondition {

  private final String function;
  private final IsExpression expression;
  private final IsExpression[] values;

  public FunctionCondition(String func, IsExpression expr, IsExpression... vals) {
    Assert.notEmpty(func);
    Assert.notEmpty(expr);
    Assert.arrayLength(vals, 1);

    function = func;
    expression = expr;
    values = vals;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (IsExpression e : values) {
      List<Object> eList = e.getSqlParams();

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

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder sb = new StringBuilder();

    sb.append(function).append("(").append(
        expression.getSqlString(builder, false));

    for (IsExpression val : values) {
      sb.append(", ").append(val.getSqlString(builder, paramMode));
    }
    return sb.append(")").toString();
  }
}
