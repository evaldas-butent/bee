package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.Operator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class FullTextCondition implements IsCondition {

  private final IsExpression expression;
  private final String value;

  FullTextCondition(IsExpression expression, String value) {
    this.expression = Assert.notNull(expression);
    this.value = Assert.notEmpty(value);
  }

  @Override
  public IsCondition copyOf() {
    return new FullTextCondition(expression, value);
  }

  @Override
  public Collection<String> getSources() {
    return null;
  }

  @Override
  public String getSqlString(SqlBuilder builder) {
    Assert.notNull(builder);
    Map<String, String> params = new HashMap<>();
    params.put("expression", expression.getSqlString(builder));
    params.put("value0", value);

    return builder.sqlCondition(Operator.FULL_TEXT, params);
  }
}
