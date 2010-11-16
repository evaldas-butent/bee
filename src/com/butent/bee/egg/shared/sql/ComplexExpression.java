package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class ComplexExpression implements IsExpression {

  private final Object[] content;

  public ComplexExpression(Object... expr) {
    Assert.arrayLengthMin(expr, 1);
    Assert.noNulls(expr);

    content = expr;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (Object o : content) {
      if (o instanceof IsSql) {
        SqlUtils.addParams(paramList, ((IsSql) o).getSqlParams());
      }
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder s = new StringBuilder();

    for (Object o : content) {
      if (o instanceof IsSql) {
        s.append(((IsSql) o).getSqlString(builder, paramMode));
      } else {
        s.append(BeeUtils.transform(o));
      }
    }
    return s.toString();
  }

  @Override
  public String getValue() {
    return BeeUtils.join(content, 0);
  }
}
