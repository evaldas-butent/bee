package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Generates complex expressions for SQL statements depending on specific SQL server requirements.
 */

class CompoundExpression implements IsExpression {

  private final Object[] content;

  public CompoundExpression(Object... expr) {
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
  public Object getValue() {
    return content;
  }
}
