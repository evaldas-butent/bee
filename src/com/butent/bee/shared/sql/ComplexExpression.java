package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class ComplexExpression implements IsExpression {

  private final Object[] content;

  public ComplexExpression(Object... expr) {
    Assert.minLength(expr, 1);
    Assert.noNulls(expr);

    content = expr;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (Object o : content) {
      if (o instanceof IsSql) {
        paramList = (List<Object>) SqlUtils.addCollection(paramList, ((IsSql) o).getSqlParams());
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
        s.append(BeeUtils.transformNoTrim(o));
      }
    }
    return s.toString();
  }

  @Override
  public String getValue() {
    return ArrayUtils.join(content, 0);
  }
}
