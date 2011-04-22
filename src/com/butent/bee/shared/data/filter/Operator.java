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

  public static String getPattern(boolean multipleChars) {
    StringBuilder sb = new StringBuilder();

    for (Operator operator : Operator.values()) {
      String op = operator.toTextString();

      if (!BeeUtils.isEmpty(op)) {
        if (multipleChars && op.length() > 1 || !multipleChars && op.length() == 1) {
          if (sb.length() > 0) {
            sb.append("|");
          }
          if (LIKE == operator) {
            op = "\\" + op;
          }
          sb.append(op);
        }
      }
    }
    return sb.toString();
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
