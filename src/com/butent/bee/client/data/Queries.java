package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcParameter;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfoCollection;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * Contains methods for getting {@code RowSets} and making POST requests.
 */

public class Queries {
  
  private static final int RESPONSE_FROM_CACHE = 0;

  /**
   * Requires implementing classes to have {@code onResponse) method. 
   */
  public interface IntCallback {
    void onResponse(int value);
  }

  /**
   * Requires implementing classes to have {@code onResponse) method applied for a {@code RowSet}
   * object.
   */
  public interface RowSetCallback {
    void onResponse(BeeRowSet rowSet);
  }

  public static void deleteRow(String viewName, long rowId) {
    deleteRow(viewName, rowId, null);
  }

  public static void deleteRow(String viewName, long rowId, IntCallback callback) {
    deleteRows(viewName, Lists.newArrayList(rowId), callback);
  }
  
  public static void deleteRows(String viewName, List<Long> rowIds) {
    deleteRows(viewName, rowIds, null);
  }
  
  public static void deleteRows(final String viewName, List<Long> rowIds,
      final IntCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(rowIds);
    
    final int requestCount = rowIds.size();

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, viewName,
        Service.VAR_VIEW_ROWS, new RowInfoCollection(rowIds).serialize());
    
    BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.DELETE_ROWS,
        RpcParameter.SECTION.DATA, lst), new ResponseCallback() {
      public void onResponse(JsArrayString arr) {
        String s = arr.get(0);
        int responseCount = BeeUtils.toInt(Codec.beeDeserialize(s)[0]);
        
        if (responseCount == requestCount) {
          BeeKeeper.getLog().info(viewName, "deleted", responseCount, "rows");
        } else {
          BeeKeeper.getLog().severe(viewName, "deleted", responseCount, "rows of",
              requestCount, "requested");
        }
        if (callback != null) {
          callback.onResponse(responseCount);
        }
      }
    });
  }
  
  public static void getRowCount(final String viewName, final Filter filter,
      final IntCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(callback);

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, viewName);
    if (filter != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_WHERE, filter.serialize());
    }

    BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.COUNT_ROWS,
        RpcParameter.SECTION.DATA, lst), new ResponseCallback() {
      public void onResponse(JsArrayString arr) {
        String s = arr.get(0);
        int rowCount = BeeUtils.toInt(Codec.beeDeserialize(s)[0]);
        BeeKeeper.getLog().info(viewName, filter, "row count", rowCount);
        callback.onResponse(rowCount);
      }
    });
  }
  
  public static void getRowCount(String viewName, final IntCallback callback) {
    getRowCount(viewName, null, callback);
  }

  public static int getRowSet(String viewName, Filter filter, Order order,
      CachingPolicy cachingPolicy, RowSetCallback callback) {
    return getRowSet(viewName, filter, order, -1, -1, cachingPolicy, callback);
  }
  
  public static int getRowSet(String viewName, Filter filter, Order order,
      int offset, int limit, CachingPolicy cachingPolicy, RowSetCallback callback) {
    return getRowSet(viewName, filter, order, offset, limit, null, cachingPolicy, callback);
  }
  
  public static int getRowSet(String viewName, Filter filter, Order order,
      int offset, int limit, RowSetCallback callback) {
    return getRowSet(viewName, filter, order, offset, limit, CachingPolicy.NONE, callback);
  }
  
  public static int getRowSet(String viewName, final Filter filter, final Order order,
      final int offset, final int limit, String states, final CachingPolicy cachingPolicy,
      final RowSetCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(callback);

    if (cachingPolicy != null && cachingPolicy.doRead()) {
      BeeRowSet rowSet = Global.getCache().getRowSet(viewName, filter, order, offset, limit);
      if (rowSet != null) {
        callback.onResponse(rowSet);
        return RESPONSE_FROM_CACHE;
      }
    }

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, viewName);

    if (filter != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_WHERE, filter.serialize());
    }
    if (order != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_ORDER, order.serialize());
    }

    if (offset >= 0 && limit > 0) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_OFFSET, offset,
          Service.VAR_VIEW_LIMIT, limit);
    }

    if (!BeeUtils.isEmpty(states)) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_STATES, states);
    }

    return BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.QUERY,
        RpcParameter.SECTION.DATA, lst), new ResponseCallback() {
      public void onResponse(JsArrayString arr) {
        BeeRowSet rs = BeeRowSet.restore(arr.get(0));
        callback.onResponse(rs);
        if (cachingPolicy != null && cachingPolicy.doWrite()) {
          Global.getCache().add(rs, filter, order, offset, limit);
        }
      }
    });
  }
  
  public static int getRowSet(String viewName, Filter filter, Order order,
      RowSetCallback callback) {
    return getRowSet(viewName, filter, order, CachingPolicy.NONE, callback);
  }

  public static int getRowSet(String viewName, Filter filter, RowSetCallback callback) {
    return getRowSet(viewName, filter, null, callback);
  }

  public static int getRowSet(String viewName, Order order, RowSetCallback callback) {
    return getRowSet(viewName, null, order, callback);
  }
  
  public static int getRowSet(String viewName, RowSetCallback callback) {
    return getRowSet(viewName, null, null, callback);
  }
  
  public static boolean isResponseFromCache(int id) {
    return id == RESPONSE_FROM_CACHE;
  }

  private Queries() {
  }
}
