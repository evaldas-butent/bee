package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

class FieldExpression implements Expression {

  private final String source, field;

  public FieldExpression(String field) {
    this(null, field);
  }

  public FieldExpression(String source, String field) {
    Assert.notEmpty(field);

    this.source = source;
    this.field = field;
  }

  @Override
  public String getExpression(SqlBuilder builder, boolean paramMode) {
    String s = builder.sqlQuote(field);

    if (!BeeUtils.isEmpty(source)) {
      s = builder.sqlQuote(source) + "." + s;
    }
    return s;
  }

  @Override
  public List<Object> getParameters() {
    return null;
  }

  String getField() {
    return field;
  }

  String getSource() {
    return source;
  }
}
