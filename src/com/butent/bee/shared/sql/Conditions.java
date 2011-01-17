package com.butent.bee.shared.sql;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Conditions implements IsCondition {
  private List<IsCondition> conditionList = new ArrayList<IsCondition>();

  public void add(IsCondition... conditions) {
    for (IsCondition cond : conditions) {
      if (!BeeUtils.isEmpty(cond)) {
        conditionList.add(cond);
      }
    }
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (IsCondition cond : conditionList) {
      SqlUtils.addParams(paramList, cond.getSqlParams());
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

  protected abstract String joinMode();
}
