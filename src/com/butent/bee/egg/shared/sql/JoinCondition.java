package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class JoinCondition implements Condition {
  private String leftExpression;
  private String operator;
  private Object rightExpression;

  public JoinCondition(String left, String op, SqlSelect right) {
    this(left, op);

    Assert.notNull(right);
    Assert.state(!right.isEmpty());

    rightExpression = right;
  }

  public JoinCondition(String left, String op, String right) {
    this(left, op);

    Assert.notEmpty(right);

    rightExpression = right;
  }

  private JoinCondition(String left, String op) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);

    leftExpression = left;
    operator = op;
  }

  @Override
  public String getCondition(SqlBuilder builder, boolean paramMode) {
    String cond = leftExpression + operator;
    Object expr = rightExpression;

    if (expr instanceof SqlSelect) {
      expr = "(" + ((SqlSelect) expr).getQuery(builder, paramMode) + ")";
    } else {
      expr = BeeUtils.transform(expr);
    }
    return cond + expr;
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
    }
    return paramList;
  }
}
