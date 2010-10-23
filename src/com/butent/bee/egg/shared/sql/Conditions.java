package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Conditions implements Condition {
  private List<Condition> conditionList = new ArrayList<Condition>();

  public void add(Condition... conditions) {
    Assert.noNulls((Object[]) conditions);

    for (Condition cond : conditions) {
      conditionList.add(cond);
    }
  }

  // Implementations ----------------------------------------------------------
  @Override
  public String getCondition(SqlBuilder builder, boolean paramMode) {
    StringBuilder clause = new StringBuilder();

    for (int i = 0; i < conditionList.size(); i++) {
      Condition cond = conditionList.get(i);
      if (i > 0) {
        clause.append(joinMode());
      }
      clause.append(cond.getCondition(builder, paramMode));
    }
    return clause.toString();
  }

  @Override
  public List<Object> getParameters() {
    List<Object> param = null;

    for (Condition cond : conditionList) {
      List<Object> cList = cond.getParameters();

      if (!BeeUtils.isEmpty(cList)) {
        if (!BeeUtils.isEmpty(cList)) {
          param = cList;
        } else {
          param.addAll(cList);
        }
      }
    }
    return param;
  }

  protected abstract String joinMode();
}
