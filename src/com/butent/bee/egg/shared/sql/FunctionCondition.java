package com.butent.bee.egg.shared.sql;

import java.util.ArrayList;
import java.util.List;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class FunctionCondition implements Condition {
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
  public String getCondition(boolean queryMode) {
    StringBuilder sb = new StringBuilder();
    sb.append(function).append("(").append(expression);

    if (!BeeUtils.isEmpty(values)) {
      for (Object val : values) {
        sb.append(", ").append(queryMode ? "?" : BeeUtils.transform(val));
      }
    }
    return sb.append(")").toString();
  }

  @Override
  public List<Object> getQueryParameters() {
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
