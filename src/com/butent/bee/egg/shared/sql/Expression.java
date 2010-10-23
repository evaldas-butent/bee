package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class Expression {

  private final Object[] content;

  public Expression(Object... expr) {
    Assert.arrayLength(expr, 1);
    Assert.noNulls(expr);

    content = expr;
  }

  public String getExpression(SqlBuilder builder) {
    StringBuilder s = new StringBuilder();

    for (Object o : content) {
      if (o instanceof Expression) {
        s.append(((Expression) o).getExpression(builder));
      } else {
        s.append(BeeUtils.transform(o));
      }
    }
    return s.toString();
  }
}
