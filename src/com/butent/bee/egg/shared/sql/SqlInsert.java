package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;

import java.util.Map;

public class SqlInsert extends SqlQuery {

  @Override
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  Map<Integer, Object> getParameters() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  String getQuery(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);

    return builder.getInsert(this, paramMode);
  }
}
