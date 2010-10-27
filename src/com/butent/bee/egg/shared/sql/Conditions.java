package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Conditions implements IsCondition {
  private List<IsCondition> conditionList = new ArrayList<IsCondition>();

  public void add(IsCondition... conditions) {
    Assert.noNulls((Object[]) conditions);

    for (IsCondition cond : conditions) {
      conditionList.add(cond);
    }
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (IsCondition cond : conditionList) {
      List<Object> cList = cond.getSqlParams();

      if (!BeeUtils.isEmpty(cList)) {
        if (BeeUtils.isEmpty(paramList)) {
          paramList = cList;
        } else {
          paramList.addAll(cList);
        }
      }
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder clause = new StringBuilder();

    for (int i = 0; i < conditionList.size(); i++) {
      IsCondition cond = conditionList.get(i);
      if (i > 0) {
        clause.append(joinMode());
      }
      clause.append(cond.getSqlString(builder, paramMode));
    }
    return clause.toString();
  }

  protected abstract String joinMode();
}
