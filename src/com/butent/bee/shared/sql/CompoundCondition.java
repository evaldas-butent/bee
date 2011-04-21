package com.butent.bee.shared.sql;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.CompoundFilter.JoinType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

public class CompoundCondition implements IsCondition {

  static CompoundCondition and(IsCondition... conditions) {
    return new CompoundCondition(JoinType.AND, conditions);
  }

  static CompoundCondition or(IsCondition... conditions) {
    return new CompoundCondition(JoinType.OR, conditions);
  }

  private final JoinType joinType;
  private final List<IsCondition> subConditions = Lists.newArrayList();

  private CompoundCondition(JoinType joinType, IsCondition... conditions) {
    Assert.notEmpty(joinType);
    this.joinType = joinType;
    add(conditions);
  }

  public void add(IsCondition... conditions) {
    if (!BeeUtils.isEmpty(conditions)) {
      for (IsCondition cond : conditions) {
        if (!BeeUtils.isEmpty(cond)) {
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
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (IsCondition cond : subConditions) {
      paramList = (List<Object>) SqlUtils.addCollection(paramList, cond.getSqlParams());
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder sb = new StringBuilder();

    for (IsCondition cond : subConditions) {
      String expr = cond.getSqlString(builder, paramMode);

      if (!BeeUtils.isEmpty(expr) && sb.length() > 0) {
        sb.append(" ").append(joinType).append(" ");
      }
      sb.append(expr);
    }
    String flt = sb.toString();

    if (JoinType.OR == joinType && !BeeUtils.isEmpty(flt)) {
      flt = BeeUtils.parenthesize(flt);
    }
    return flt;
  }
}
