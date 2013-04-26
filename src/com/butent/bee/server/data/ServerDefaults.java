package com.butent.bee.server.data;

import com.butent.bee.shared.data.Defaults;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ServerDefaults extends Defaults {

  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;

  @Override
  public Object getValue(DefaultExpression defExpr, Object defValue) {
    return getValue(null, null, defExpr, defValue);
  }

  public Object getValue(String tblName, String fldName, DefaultExpression defExpr, Object defValue) {
    Object value = null;

    if (defExpr == null) {
      value = defValue;
    } else {
      switch (defExpr) {
        case CURRENT_USER:
          value = usr.getCurrentUserId();
          break;

        case NEXT_NUMBER:
          String prefix = (defValue == null) ? null : defValue.toString().trim();
          value = qs.getNextNumber(tblName, fldName, prefix, null);
          break;

        default:
          value = super.getValue(defExpr, defValue);
          break;
      }
    }
    return value;
  }
}
