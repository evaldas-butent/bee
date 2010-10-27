package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class ComplexExpression implements IsExpression {

  private final Object[] content;

  public ComplexExpression(Object... expr) {
    Assert.arrayLength(expr, 1);
    Assert.noNulls(expr);

    content = expr;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (Object o : content) {
      if (o instanceof IsSql) {
        List<Object> eList = ((IsSql) o).getSqlParams();

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
}
