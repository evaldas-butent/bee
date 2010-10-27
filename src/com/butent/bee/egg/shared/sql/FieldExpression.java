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
    String s;

    if (BeeUtils.isEmpty(source)) {
      s = builder.sqlQuote(field);
    } else {
      s = builder.sqlQuote(source) + "." + builder.sqlQuote(field);
    }
    return s;
  }

  String getField() {
    return field;
  }

  String getSource() {
    return source;
  }
}
