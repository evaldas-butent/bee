package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;

import java.util.List;

class JoinCondition implements IsCondition {

  private final IsExpression leftExpression;
  private final String operator;
  private final Object rightExpression;

  public JoinCondition(IsExpression left, String op, IsExpression right) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);

    leftExpression = left;
    operator = op;

    Assert.notEmpty(right);

    rightExpression = right;
  }

  public JoinCondition(IsExpression left, String op, SqlSelect right) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);

    leftExpression = left;
    operator = op;

    Assert.notNull(right);
    Assert.state(!right.isEmpty());

    rightExpression = right;
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
