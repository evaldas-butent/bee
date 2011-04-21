package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;

import java.util.Collection;
import java.util.List;

class ComparisonCondition implements IsCondition {

  private final IsExpression leftExpression;
  private final String operator;
  private final Object rightExpression;

  public ComparisonCondition(IsExpression left, String op, IsExpression right) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);
    Assert.notEmpty(right);

    leftExpression = left;
    operator = op;
    rightExpression = right;
  }

  public ComparisonCondition(IsExpression left, String op, SqlSelect right) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);
    Assert.notNull(right);
    Assert.state(!right.isEmpty());

    leftExpression = left;
    operator = op;
    rightExpression = right;
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = null;

    if (rightExpression instanceof HasSource) {
      sources = ((HasSource) rightExpression).getSources();
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    return ((IsSql) rightExpression).getSqlParams();
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    String expr = ((IsSql) rightExpression).getSqlString(builder, paramMode);

    if (rightExpression instanceof SqlSelect) {
      expr = "(" + expr + ")";
    }
    return leftExpression.getSqlString(builder, false) + operator + expr;
  }
}
