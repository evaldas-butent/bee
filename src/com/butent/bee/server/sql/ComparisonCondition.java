package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.Operator;

import java.util.Collection;
import java.util.List;

/**
 * Generates comparison condition parts for SQL statements depending on specific SQL server
 * requirements.
 * 
 */

class ComparisonCondition implements IsCondition {

  private final IsExpression leftExpression;
  private final Operator operator;
  private final Object rightExpression;

  public ComparisonCondition(IsExpression left, Operator op, IsExpression right) {
    Assert.noNulls(left, op, right);

    leftExpression = left;
    operator = op;
    rightExpression = right;
  }

  public ComparisonCondition(IsExpression left, Operator op, SqlSelect right) {
    Assert.noNulls(left, op, right);
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
    return leftExpression.getSqlString(builder, false) + operator.toSqlString() + expr;
  }
}
