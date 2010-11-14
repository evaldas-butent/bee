package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class FieldExpression implements IsExpression {

  private final String source, field;

  public FieldExpression(String source, String field) {
    Assert.notEmpty(field);

    this.source = source;
    this.field = field;
  }

  @Override
  public List<Object> getSqlParams() {
    return null;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder s = new StringBuilder();

    if (BeeUtils.isEmpty(source)) {
      s.append(builder.sqlQuote(field));
    } else {
      String[] arr = ((String) source).split("\\.");

      for (int i = 0; i < arr.length; i++) {
        if (i > 0) {
          s.append(".");
        }
        s.append(builder.sqlQuote(arr[i]));
      }
      s.append(".").append(builder.sqlQuote(field));
    }
    return s.toString();
  }

  String getField() {
    return field;
  }

  String getSource() {
    return source;
  }
}
