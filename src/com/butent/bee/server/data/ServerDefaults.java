package com.butent.bee.server.data;

import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.Defaults;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.utils.BeeUtils;

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
          value = getNextNumber(tblName, fldName, BeeUtils.transform(defValue));
          break;

        default:
          value = super.getValue(defExpr, defValue);
          break;
      }
    }
    return value;
  }

  private String getNextNumber(String tblName, String fldName, String prefix) {
    Object value = null;

    if (!BeeUtils.allEmpty(tblName, fldName)) {
      IsCondition clause = null;
      IsExpression xpr = null;

      if (BeeUtils.isEmpty(prefix)) {
        xpr = SqlUtils.field(tblName, fldName);
      } else {
        xpr = SqlUtils.substring(tblName, fldName, prefix.length() + 1);
        clause = SqlUtils.startsWith(tblName, fldName, prefix);
      }
      clause = SqlUtils.and(clause,
          SqlUtils.compare(SqlUtils.length(xpr), Operator.EQ,
              new SqlSelect()
                  .addMax(SqlUtils.length(xpr), "length")
                  .addFrom(tblName)
                  .setWhere(clause)));

      String maxValue = qs.getValue(new SqlSelect()
          .addMax(xpr, "value")
          .addFrom(tblName)
          .setWhere(clause));

      value = BeeUtils.nextString(maxValue);
    }
    return BeeUtils.join(BeeConst.STRING_EMPTY, prefix, value);
  }
}
