package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;

import java.util.Collection;
import java.util.List;

abstract class FromJoin extends FromSingle {

  private final IsCondition on;

  public FromJoin(String source, IsCondition on) {
    this(source, null, on);
  }

  public FromJoin(String source, String alias, IsCondition on) {
    super(source, alias);

    Assert.notNull(on);
    this.on = on;
  }

  public FromJoin(SqlSelect source, String alias, IsCondition on) {
    super(source, alias);

    Assert.notNull(on);
    this.on = on;
  }

  @Override
  public Collection<String> getSources() {
    return SqlUtils.addCollection(super.getSources(), on.getSources());
  }

  @Override
  public List<Object> getSqlParams() {
    return (List<Object>) SqlUtils.addCollection(super.getSqlParams(), on.getSqlParams());
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean queryMode) {
    StringBuilder from = new StringBuilder(super.getSqlString(builder,
        queryMode));

    from.append(" ON ").append(on.getSqlString(builder, queryMode));

    return from.toString();
  }
}
