package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class JoinCondition implements Condition {

  private final Expression leftExpression;
  private final String operator;
  private final Object rightExpression;

  public JoinCondition(Expression left, String op, SqlSelect right) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);

    leftExpression = left;
    operator = op;

    Assert.notNull(right);
    Assert.state(!right.isEmpty());

    rightExpression = right;
  }

  public JoinCondition(Expression left, String op, Expression right) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);

    leftExpression = left;
    operator = op;

    Assert.notEmpty(right);

    rightExpression = right;
  }

  @Override
  public String getCondition(SqlBuilder builder, boolean paramMode) {
    Object expr = rightExpression;

    if (expr instanceof SqlSelect) {
      expr = "(" + ((SqlSelect) expr).getQuery(builder, paramMode) + ")";
    } else {
      expr = ((Expression) expr).getExpression(builder, paramMode);
    }
    return leftExpression.getExpression(builder, false) + operator + expr;
  }

  @Override
  public List<Object> getParameters() {
    List<Object> paramList = null;

    if (rightExpression instanceof SqlSelect) {
      Map<Integer, Object> paramMap = ((SqlSelect) rightExpression).getParameters();

      if (!BeeUtils.isEmpty(paramMap)) {
        paramList = new ArrayList<Object>(paramMap.size());

        for (int i = 0; i < paramMap.size(); i++) {
          paramList.add(paramMap.get(i + 1));
        }
      }
    } else {
      paramList = ((Expression) rightExpression).getParameters();
    }
    return paramList;
  }
}
