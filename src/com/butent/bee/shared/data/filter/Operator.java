package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.utils.BeeUtils;

public enum Operator {
  EQ("="),
  NE("!="),
  LT("<"),
  GT(">"),
  LE("<="),
  GE(">="),
  LIKE("$", " LIKE "),
  IN(null, " IN "),
  IS(null, " IS ");

  public static Operator detectOperator(String expr) {
    Operator op = null;
  
    if (!BeeUtils.isEmpty(expr)) {
      for (Operator operator : Operator.values()) {
        String s = operator.toTextString();
  
        if (!BeeUtils.isEmpty(s)) {
          if (expr.startsWith(s) && (op == null || s.length() > op.toTextString().length())) {
            op = operator;
          }
        }
      }
    }
    return op;
  }

  public static Operator getOperator(String op) {
    if (!BeeUtils.isEmpty(op)) {
      for (Operator operator : Operator.values()) {
        if (BeeUtils.same(operator.toTextString(), op)) {
          return operator;
        }
      }
    }
    return null;
  }

  private String textString;
  private String sqlString;

  Operator(String textString) {
    this(textString, null);
  }

  Operator(String textString, String sqlString) {
    this.textString = textString;
    this.sqlString = sqlString;
  }

  public String toSqlString() {
    return BeeUtils.ifString(sqlString, textString);
  }

  public String toTextString() {
    return textString;
  }
}
