package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.utils.BeeUtils;

public enum CompoundType {
    AND(" AND "),
    OR(" OR "),
    NOT("!", "NOT");

  private String textString;
  private String sqlString;

  CompoundType(String textString) {
    this(textString, null);
  }

  CompoundType(String textString, String sqlString) {
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
