package com.butent.bee.egg.client.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.communication.ParameterList;
import com.butent.bee.egg.client.communication.ResponseCallback;
import com.butent.bee.egg.client.utils.BeeJs;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class CacheUtils {
  private class PrimaryKeyCallback implements ResponseCallback {
    private String table;
    private ResponseCallback callback;

    public PrimaryKeyCallback(String table, ResponseCallback callback) {
      this.table = table;
      this.callback = callback;
    }

    @Override
    public void onResponse(JsArrayString arr) {
      Assert.notNull(arr);
      Assert.isTrue(arr.length() >= 1);
      String column = arr.get(0);
      Assert.notEmpty(column);
      
      setPrimaryKey(table, column);
      BeeKeeper.getLog().info(table, column);

      callback.onResponse(arr);
    }
  }
  
  private static String primaryKeyPrefix = "pk-";
  
  public void getPrimaryKey(String table, ResponseCallback callback) {
    Assert.notEmpty(table);
    Assert.notNull(callback);
    
    String value = BeeKeeper.getStorage().getItem(pkName(table));
    if (!BeeUtils.isEmpty(value)) {
      callback.onResponse(BeeJs.createArray(value));
      return;
    }
    
    ParameterList params = BeeKeeper.getRpc().createParameters(BeeService.SERVICE_DB_PRIMARY);
    params.addPositionalHeader(table);
    BeeKeeper.getRpc().makeGetRequest(params, new PrimaryKeyCallback(table, callback));
  }

  public void setPrimaryKey(String ref) {
    Assert.notEmpty(ref);
    String table = BeeUtils.getPrefix(ref, BeeConst.CHAR_POINT);
    String column = BeeUtils.getSuffix(ref, BeeConst.CHAR_POINT);
    
    setPrimaryKey(table, column);
  }
  
  public void setPrimaryKey(String table, String column) {
    Assert.notEmpty(table);
    Assert.notEmpty(column);
    
    BeeKeeper.getStorage().setItem(pkName(table), column.trim());
  }
  
  private String pkName(String table) {
    return primaryKeyPrefix + table.trim().toLowerCase();
  }

}
