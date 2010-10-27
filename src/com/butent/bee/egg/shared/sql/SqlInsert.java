package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;

import java.util.List;

public class SqlInsert extends SqlQuery<SqlInsert> {

  @Override
  public List<Object> getSqlParams() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);

    return builder.getInsert(this, paramMode);
  }

  @Override
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected SqlInsert getReference() {
    return this;
  }
}
