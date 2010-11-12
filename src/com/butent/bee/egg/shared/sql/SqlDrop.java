package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.List;

public class SqlDrop extends SqlQuery<SqlDrop> {

  private final IsFrom target;

  public SqlDrop(String target) {
    this.target = new FromSingle(target);
  }

  @Override
  public List<Object> getSqlParams() {
    return null;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getDrop(this, paramMode);
  }

  public IsFrom getTarget() {
    return target;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target);
  }

  @Override
  protected SqlDrop getReference() {
    return this;
  }
}
