package com.butent.bee.server.sql;

import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Generates comparison condition parts for SQL statements depending on specific SQL server
 * requirements.
 * 
 */

class ComparisonCondition implements IsCondition {

  private final Operator operator;
  private final IsExpression expression;
  private final IsSql[] values;

  public ComparisonCondition(Operator operator, IsExpression expression, IsSql... values) {
    Assert.noNulls(operator, expression);

    this.operator = operator;
    this.expression = expression;
    this.values = values;
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = null;

    if (!BeeUtils.isEmpty(values)) {
      for (IsSql value : values) {
        if (value instanceof HasSource) {
          sources = SqlUtils.addCollection(sources, ((HasSource) value).getSources());
        }
      }
    }
    return sources;
  }

  @Override
  public String getSqlString(SqlBuilder builder) {
    Assert.notEmpty(builder);
    Map<String, String> params = Maps.newHashMap();
    params.put("expression", expression.getSqlString(builder));

    if (!BeeUtils.isEmpty(values)) {
      int i = 0;

      for (IsSql value : values) {
        String val = value.getSqlString(builder);

        if (value instanceof SqlSelect) {
          val = BeeUtils.parenthesize(val);
        }
        params.put("value" + i++, val);
      }
    }
    return builder.sqlCondition(operator, params);
  }
}
