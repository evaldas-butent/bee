package com.butent.bee.client.utils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Handles creation of Javascript objects and conversion of other types of objects into them.
 */

public final class EvalHelper {

  public static JavaScriptObject createJso(List<? extends IsColumn> columns) {
    Assert.notNull(columns);

    JavaScriptObject jso = JavaScriptObject.createObject();
    for (int i = 0; i < columns.size(); i++) {
      JsUtils.setPropertyToNull(jso, columns.get(i).getId());
    }
    return jso;
  }

  public static void setJsoProperty(JavaScriptObject jso, String name, ValueType type,
      String value) {
    if (value == null || value.trim().isEmpty()) {
      JsUtils.setPropertyToNull(jso, name);
      return;
    }

    switch (type) {
      case BOOLEAN:
        Boolean b = BeeUtils.toBooleanOrNull(value);
        if (b == null) {
          JsUtils.setPropertyToNull(jso, name);
        } else {
          JsUtils.setProperty(jso, name, BeeUtils.unbox(b));
        }
        break;

      case DATE:
        JustDate jd = TimeUtils.toDateOrNull(value);
        if (jd == null) {
          JsUtils.setPropertyToNull(jso, name);
        } else {
          JsUtils.setProperty(jso, name, toJs(jd));
        }
        break;

      case DATE_TIME:
        DateTime dt = TimeUtils.toDateTimeOrNull(value);
        if (dt == null) {
          JsUtils.setPropertyToNull(jso, name);
        } else {
          JsUtils.setProperty(jso, name, toJs(dt));
        }
        break;

      case DECIMAL:
      case INTEGER:
      case LONG:
      case NUMBER:
        if (BeeUtils.isDouble(value)) {
          JsUtils.setProperty(jso, name, BeeUtils.toDouble(value));
        } else {
          JsUtils.setPropertyToNull(jso, name);
        }
        break;

      case TEXT:
      case BLOB:
      case TIME_OF_DAY:
        JsUtils.setProperty(jso, name, value);
        break;
    }
  }

  public static void toJso(List<? extends IsColumn> columns, IsRow row, JavaScriptObject jso) {
    Assert.notNull(columns);
    Assert.notNull(row);
    Assert.notNull(jso);

    for (int i = 0; i < columns.size(); i++) {
      IsColumn column = columns.get(i);
      setJsoProperty(jso, column.getId(), column.getType(), row.getString(i));
    }
  }

  private static JsDate toJs(DateTime date) {
    if (date == null) {
      return null;
    }
    return JsDate.create(date.getTime());
  }

  private static JsDate toJs(JustDate date) {
    if (date == null) {
      return null;
    }
    return JsDate.create(date.getYear(), date.getMonth() - 1, date.getDom());
  }

  private EvalHelper() {
  }
}
