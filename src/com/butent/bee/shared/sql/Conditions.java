package com.butent.bee.shared.sql;

import com.google.common.collect.Lists;

import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;

public abstract class Conditions extends Condition {

  private final List<IsCondition> conditionList = Lists.newArrayList();

  public void add(IsCondition... conditions) {
    for (IsCondition cond : conditions) {
      if (!BeeUtils.isEmpty(cond)) {
        conditionList.add(cond);
      }
    }
  }

  @Override
  public void deserialize(String s) {
    setSafe();
    for (String cond : Codec.beeDeserialize(s)) {
      conditionList.add(Condition.restore(cond));
    }
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = null;

    for (IsCondition condition : conditionList) {
      sources = SqlUtils.addCollection(sources, condition.getSources());
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (IsCondition cond : conditionList) {
      paramList = (List<Object>) SqlUtils.addCollection(paramList, cond.getSqlParams());
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder clause = new StringBuilder();

    for (IsCondition cond : conditionList) {
      String expr = cond.getSqlString(builder, paramMode);

      if (!BeeUtils.isEmpty(expr) && clause.length() > 0) {
        clause.append(joinMode());
      }
      clause.append(expr);
    }
    return clause.toString();
  }

  @Override
  public String serialize() {
    return Codec.beeSerializeAll(BeeUtils.getClassName(this.getClass()), conditionList);
  }

  protected abstract String joinMode();
}
