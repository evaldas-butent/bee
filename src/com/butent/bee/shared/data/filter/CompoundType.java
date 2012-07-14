package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Contains a list of possible constructors for complex filter expressions (AND, OR, NOT).
 */

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
    return BeeUtils.notEmpty(sqlString, textString);
  }

  public String toTextString() {
    return textString;
  }
}
