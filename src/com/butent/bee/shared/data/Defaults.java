package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.time.TimeUtils;

public abstract class Defaults {

  public enum DefaultExpression {
    CURRENT_DATE, CURRENT_TIME, CURRENT_USER, NEXT_NUMBER, MAIN_CURRENCY
  }

  public Object getValue(DefaultExpression defExpr, Object defValue) {
    Object value = null;

    if (defExpr == null) {
      value = defValue;
    } else {
      switch (defExpr) {
        case CURRENT_DATE:
          value = TimeUtils.today();
          break;

        case CURRENT_TIME:
          value = TimeUtils.nowMinutes();
          break;

        case CURRENT_USER:
        case NEXT_NUMBER:
        case MAIN_CURRENCY:
          Assert.unsupported();
          break;
      }
    }
    return value;
  }
}
