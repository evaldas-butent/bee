package com.butent.bee.client.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcParameter;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.Filter;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class Queries {
  public interface IntCallback {
    void onResponse(int value);
  }

  public interface RowSetCallback {
    void onResponse(BeeRowSet rowSet);
  }

  public static void getRowSet(String viewName, Filter condition, String order,
      int offset, int limit, RowSetCallback callback) {
    getRowSet(viewName, condition, order, offset, limit, null, callback);
  }

  public static void getRowSet(String viewName, Filter condition, String order,
      int offset, int limit, String states, final RowSetCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(callback);

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, viewName);

    if (condition != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_WHERE, Codec.beeSerialize(condition));
    }
    if (!BeeUtils.isEmpty(order)) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_ORDER, order);
    }

    if (offset >= 0 && limit > 0) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_OFFSET, offset,
          Service.VAR_VIEW_LIMIT, limit);
    }

    if (!BeeUtils.isEmpty(states)) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_STATES, states);
    }

    BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.QUERY,
        RpcParameter.SECTION.DATA, lst), new ResponseCallback() {
      public void onResponse(JsArrayString arr) {
        callback.onResponse(BeeRowSet.restore(arr.get(0)));
      }
    });
  }

  public static void getRowCount(String viewName, Filter condition,
      final IntCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(callback);

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, viewName);
    if (condition != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_WHERE, Codec.beeSerialize(condition));
    }

    BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.COUNT_ROWS,
        RpcParameter.SECTION.DATA, lst), new ResponseCallback() {
      public void onResponse(JsArrayString arr) {
        String s = arr.get(0);
        callback.onResponse(BeeUtils.toInt(Codec.beeDeserialize(s)[0]));
      }
    });
  }

  private Queries() {
  }
}
