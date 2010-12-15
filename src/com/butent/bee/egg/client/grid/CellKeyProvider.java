package com.butent.bee.egg.client.grid;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.view.client.ProvidesKey;

import com.butent.bee.egg.client.communication.ResponseCallback;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.HasTabularData;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class CellKeyProvider implements ProvidesKey<Integer>, ResponseCallback {
  private HasTabularData view;
  private int keyColumn = -1;
  private String keyName = null;

  public CellKeyProvider(HasTabularData view) {
    this.view = view;
  }

  public Object getKey(Integer item) {
    if (item != null && keyColumn >= 0) {
      return view.getValue(item, keyColumn);
    } else {
      return null;
    }
  }
  
  public int getKeyColumn() {
    return keyColumn;
  }

  public String getKeyName() {
    return keyName;
  }

  public void onResponse(JsArrayString arr) {
    Assert.notNull(arr);
    String name = arr.get(0);
    
    for (int i = 0; i < view.getColumnNames().length; i++) {
      if (BeeUtils.same(view.getColumnNames()[i], name)) {
        keyColumn = i;
        keyName = name;
        break;
      }
    }
  }

  public void setKeyColumn(int keyColumn) {
    this.keyColumn = keyColumn;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }
}
