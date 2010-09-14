package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class ExpressionCondition implements Condition {
  private String expression;
  private String operator;
  private Object value;

  public ExpressionCondition(String expr, String op, Object val) {
    Assert.notEmpty(expr);
    Assert.notEmpty(op);

    expression = expr;
    operator = op;
    value = val;
  }

  @Override
  public String getCondition(boolean queryMode) {
    return expression + operator
        + (queryMode ? "?" : BeeUtils.transform(value));
  }

  @Override
  public List<Object> getQueryParameters() {
    List<Object> param = new ArrayList<Object>(1);
    param.add(value);
    return param;
  }
}
