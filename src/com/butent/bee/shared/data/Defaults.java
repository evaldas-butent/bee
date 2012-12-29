package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.time.TimeUtils;

public abstract class Defaults {

  public enum DefaultExpression {
    CURRENT_DATE, CURRENT_TIME, CURRENT_USER, NEXT_NUMBER
  }

  public Object getValue(DefaultExpression defExpr, Object defValue) {
    Object value = null;

    if (defExpr == null) {
      value = defValue;
    } else {
      switch (defExpr) {
        case CURRENT_DATE:
          value = TimeUtils.today().getDays();
          break;

        case CURRENT_TIME:
          value = TimeUtils.nowMinutes().getTime();
          break;

        case CURRENT_USER:
        case NEXT_NUMBER:
          Assert.unsupported();
          break;
      }
    }
    return value;
  }
}
