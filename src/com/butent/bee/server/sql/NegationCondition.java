package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.CompoundType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Forms a logically negative expression for a given one according to 
 * requirements of specific SQL servers.
 */

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
