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
  public String getCondition(SqlBuilder builder, boolean queryMode) {
    StringBuilder clause = new StringBuilder();
    String join = joinMode();

    for (int i = 0; i < conditionList.size(); i++) {
      Condition cond = conditionList.get(i);
      if (i > 0) {
        clause.append(join);
      }
      clause.append(cond.getCondition(builder, queryMode));
    }
    return clause.toString();
  }

  @Override
  public List<Object> getQueryParameters() {
    List<Object> param = null;

    for (Condition cond : conditionList) {
      List<Object> cList = cond.getQueryParameters();

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
