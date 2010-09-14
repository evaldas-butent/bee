package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.utils.BeeUtils;

public class OrConditions extends Conditions {

  @Override
  public String getCondition(boolean queryMode) {
    String cond = super.getCondition(queryMode);

    if (!BeeUtils.isEmpty(cond)) {
      cond = "(" + cond + ")";
    }

    return cond;
  }

  @Override
  protected String joinMode() {
    return " OR ";
  }
}
