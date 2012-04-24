package com.butent.bee.server.data;

import com.butent.bee.shared.data.Defaults;

import javax.ejb.EJB;

public class ServerDefaults extends Defaults {

  @EJB
  UserServiceBean usr;

  public Object parseExpr(DefaultExpression defExpr, Object defValue) {
    Object value = null;

    if (defExpr == null) {
      value = defValue;
    } else {
      switch (defExpr) {
        case CURRENT_USER:
          value = usr.getCurrentUserId();
          break;

        default:
          value = super.getValue(defExpr, defValue);
          break;
      }
    }
    return value;
  }
}
