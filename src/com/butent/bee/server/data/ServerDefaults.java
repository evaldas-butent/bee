package com.butent.bee.server.data;

import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.Defaults;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.Map;

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

  private Object getNextNumber(String tblName, String fldName, String prefix) {
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
      Map<String, String> row = qs.getRow(new SqlSelect()
          .addMax(SqlUtils.length(xpr), "length")
          .addMax(SqlUtils.cast(xpr, SqlDataType.LONG, 0, 0), "value")
          .addMax(SqlUtils.left(xpr, 3), "left")
          .addMax(SqlUtils.right(xpr, 3), "right")
          .addFrom(tblName)
          .setWhere(clause));

      LogUtils.severe(LogUtils.getDefaultLogger(), BeeUtils.transformMap(row));

      value = BeeUtils.nextString(BeeUtils.padLeft(row.get("value"),
          BeeUtils.max(BeeUtils.toInt(row.get("length")), 1), '0'));
    }
    return BeeUtils.concat(0, prefix, value);
  }
}
