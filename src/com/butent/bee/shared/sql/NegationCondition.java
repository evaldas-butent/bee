package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.CompoundType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class NegationCondition implements IsCondition {

  private final IsCondition condition;

  public NegationCondition(IsCondition condition) {
    Assert.notNull(condition);
    this.condition = condition;
  }

  @Override
  public Collection<String> getSources() {
    return condition.getSources();
  }

  @Override
  public List<Object> getSqlParams() {
    return condition.getSqlParams();
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    return CompoundType.NOT.toSqlString()
        + BeeUtils.parenthesize(condition.getSqlString(builder, paramMode));
  }
}
