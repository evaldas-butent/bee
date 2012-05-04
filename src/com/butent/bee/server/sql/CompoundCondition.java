package com.butent.bee.server.sql;

import com.google.common.collect.Lists;

import com.butent.bee.shared.data.filter.CompoundType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Generates complex condition expressions for SQL statements depending on specific SQL server
 * requirements.
 */

class CompoundCondition implements HasConditions {

  static CompoundCondition and(IsCondition... conditions) {
    return new CompoundCondition(CompoundType.AND, conditions);
  }

  static CompoundCondition or(IsCondition... conditions) {
    return new CompoundCondition(CompoundType.OR, conditions);
  }

  private final CompoundType joinType;
  private final List<IsCondition> subConditions = Lists.newArrayList();

  private CompoundCondition(CompoundType joinType, IsCondition... conditions) {
    this.joinType = joinType;
    add(conditions);
  }

  @Override
  public void add(IsCondition... conditions) {
    if (!BeeUtils.isEmpty(conditions)) {
      for (IsCondition cond : conditions) {
        if (cond != null) {
          subConditions.add(cond);
        }
      }
    }
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = null;

    for (IsCondition condition : subConditions) {
      sources = SqlUtils.addCollection(sources, condition.getSources());
    }
    return sources;
  }

  @Override
  public String getSqlString(SqlBuilder builder) {
    StringBuilder sb = new StringBuilder();

    for (IsCondition cond : subConditions) {
      String expr = cond.getSqlString(builder);

      if (!BeeUtils.isEmpty(expr) && sb.length() > 0) {
        sb.append(joinType.toSqlString());
      }
      sb.append(expr);
    }
    String flt = sb.toString();

    if (CompoundType.OR == joinType && !BeeUtils.isEmpty(flt)) {
      flt = BeeUtils.parenthesize(flt);
    }
    return flt;
  }
}
