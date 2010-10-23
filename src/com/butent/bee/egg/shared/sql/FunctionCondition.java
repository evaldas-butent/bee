package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

class FunctionCondition implements Condition {
  private String function;
  private String expression;
  private Object[] values;

  public FunctionCondition(String func, String expr, Object... vals) {
    Assert.notEmpty(func);
    Assert.notEmpty(expr);

    function = func;
    expression = expr;
    values = vals;
  }

  @Override
  public String getCondition(SqlBuilder builder, boolean paramMode) {
    StringBuilder sb = new StringBuilder();
    sb.append(function).append("(").append(expression);

    if (!BeeUtils.isEmpty(values)) {
      for (Object val : values) {
        sb.append(", ").append(paramMode ? "?" : builder.sqlTransform(val));
      }
    }
    return sb.append(")").toString();
  }

  @Override
  public List<Object> getParameters() {
    List<Object> param = null;

    if (!BeeUtils.isEmpty(values)) {
      param = new ArrayList<Object>(values.length);

      for (Object val : values) {
        param.add(val);
      }
    }
    return param;
  }
}
