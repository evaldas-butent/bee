package com.butent.bee.client.data;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.data.Defaults;

public class ClientDefaults extends Defaults {

  @Override
  public Object getValue(DefaultExpression defExpr, Object defValue) {
    Object value = null;

    if (defExpr == null) {
      value = defValue;
    } else {
      switch (defExpr) {
        case CURRENT_USER:
          value = BeeKeeper.getUser().getUserId();
          break;

        case NEXT_NUMBER:
          break;

        default:
          value = super.getValue(defExpr, defValue);
          break;
      }
    }
    return value;
  }
}
