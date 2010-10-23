package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

class ExpressionCondition implements Condition {
  private Object expression;
  private String operator;
  private Object value;

  public ExpressionCondition(Object expr, String op, Object val) {
    Assert.notEmpty(expr);
    Assert.notEmpty(op);

    expression = expr;
    operator = op;
    value = val;
  }

  @Override
  public String getCondition(SqlBuilder builder, boolean paramMode) {
    StringBuilder s = new StringBuilder();

    if (expression instanceof Expression) {
      s.append(((Expression) expression).getExpression(builder));
    } else {
      s.append(BeeUtils.transform(expression));
    }

    s.append(operator).append(paramMode ? "?" : builder.sqlTransform(value));

    return s.toString();
  }

  @Override
  public List<Object> getParameters() {
    List<Object> param = new ArrayList<Object>(1);
    param.add(value);
    return param;
  }
}
