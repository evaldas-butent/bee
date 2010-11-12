package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlIndex extends SqlQuery<SqlIndex> {

  private final IsExpression name;
  private final IsFrom target;
  private final boolean unique;
  private IsExpression[] columns;

  public SqlIndex(String target, String name, boolean unique) {
    this.name = SqlUtils.field(name);
    this.target = new FromSingle(target);
    this.unique = unique;
  }

  public IsExpression[] getColumns() {
    if (BeeUtils.isEmpty(columns)) {
      return new IsExpression[]{getName()};
    }
    return columns;
  }

  public IsExpression getName() {
    return name;
  }

  @Override
  public List<Object> getSqlParams() {
    return null;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getIndex(this, paramMode);
  }

  public IsFrom getTarget() {
    return target;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) || BeeUtils.isEmpty(name);
  }

  public boolean isUnique() {
    return unique;
  }

  public void setColumns(String... columns) {
    Assert.arrayLengthMin(columns, 1);

    List<IsExpression> cols = new ArrayList<IsExpression>();

    for (String col : columns) {
      if (!BeeUtils.isEmpty(col)) {
        cols.add(SqlUtils.field(col));
      }
    }
    if (!BeeUtils.isEmpty(cols)) {
      this.columns = cols.toArray(new IsExpression[0]);
    }
  }

  @Override
  protected SqlIndex getReference() {
    return this;
  }
}
