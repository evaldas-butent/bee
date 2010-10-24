package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class Expressions implements Expression {

  private final Object[] content;

  public Expressions(Object... expr) {
    Assert.arrayLength(expr, 1);
    Assert.noNulls(expr);

    content = expr;
  }

  public String getExpression(SqlBuilder builder, boolean paramMode) {
    StringBuilder s = new StringBuilder();

    for (Object o : content) {
      if (o instanceof Expression) {
        s.append(((Expression) o).getExpression(builder, paramMode));
      } else {
        s.append(BeeUtils.transform(o));
      }
    }
    return s.toString();
  }

  @Override
  public List<Object> getParameters() {
    List<Object> paramList = null;

    for (Object o : content) {
      if (o instanceof Expression) {
        List<Object> eList = ((Expression) o).getParameters();

        if (!BeeUtils.isEmpty(eList)) {
          if (BeeUtils.isEmpty(paramList)) {
            paramList = eList;
          } else {
            paramList.addAll(eList);
          }
        }
      }
    }
    return paramList;
  }
}
