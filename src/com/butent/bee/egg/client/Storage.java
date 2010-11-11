package com.butent.bee.egg.client;

import com.butent.bee.egg.client.dom.Features;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.StringProp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Storage implements BeeModule {
  private Map<String, String> items = new HashMap<String, String>();
  private boolean localStorage;

  public Storage() {
    localStorage = Features.supportsLocalStorage();
  }

  public void clear() {
    if (localStorage) {
      lsClear();
    } else {
      items.clear();
    }
  }
  
  public void end() {
  }

  public List<StringProp> getAll() {
    List<StringProp> lst = new ArrayList<StringProp>();
    int len = length();
    String z;

    for (int i = 0; i < len; i++) {
      z = key(i);
      lst.add(new StringProp(z, getItem(z)));
    }
    
    return lst;
  }

  public boolean getBoolean(String key) {
    return BeeUtils.toBoolean(getItem(key));
  }

  public int getInt(String key) {
    String s = getItem(key);

    if (BeeUtils.isEmpty(s)) {
      return 0;
    } else {
      return Integer.parseInt(s);
    }
  }

  public String getItem(String key) {
    Assert.notEmpty(key);

    if (localStorage) {
      return lsGetItem(key);
    } else {
      return items.get(key);
    }
  }

  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public void init() {
  }

  public String key(int index) {
    String key = null;

    if (validIndex(index)) {
      if (localStorage) {
        key = lsKey(index);
      } else {
        int i = 0;
        for (String z : items.keySet()) {
          if (i == index) {
            key = z;
            break;
          }
          i++;
        }
      }
    }
    return key;
  }
  
  public int length() {
    if (localStorage) {
      return lsLength();
    } else {
      return items.size(); 
    }
  }
  
  public void removeItem(String key) {
    Assert.notEmpty(key);

    if (localStorage) {
      lsRemoveItem(key);
    } else {
      items.remove(key);
    }
  }

  public void setItem(String key, Object value) {
    Assert.notEmpty(key);
    String v = BeeUtils.transform(value);

    if (localStorage) {
      lsSetItem(key, value);
    } else {
      items.put(key, v);
    }
  }
  
  public void start() {
  }
  
  private native void lsClear() /*-{
    $wnd.localStorage.clear();
  }-*/;

  private native String lsGetItem(String key) /*-{
    return $wnd.localStorage.getItem(key);
  }-*/;

  private native String lsKey(int index) /*-{
    return $wnd.localStorage.key(index);
  }-*/;

  private native int lsLength() /*-{
    return $wnd.localStorage.length;
  }-*/;

  private native void lsRemoveItem(String key) /*-{
    $wnd.localStorage.removeItem(key);
  }-*/;
  
  private native void lsSetItem(String key, Object value) /*-{
    $wnd.localStorage.setItem(key, value);
  }-*/;
  
  private boolean validIndex(int index) {
    return index >= 0 && index < length();
  }
  
}
