package com.butent.bee.client;

import com.butent.bee.client.dom.Features;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * enables to store information on client side using Local Storage element from HTML5.
 * 
 * 
 */

public class Storage implements Module {
  
  private final Map<String, String> items = new HashMap<String, String>();
  private final boolean localStorage;

  public Storage() {
    this.localStorage = Features.supportsLocalStorage();
  }

  public Enum<?> checkEnum(String key, Enum<?> def) {
    Assert.notNull(def);
    String v = getItem(key);
    if (BeeUtils.isDigit(v)) {
      Enum<?>[] arr = def.getClass().getEnumConstants();
      int idx = BeeUtils.toInt(v);
      if (BeeUtils.isIndex(arr, idx)) {
        return arr[idx];
      }
    }

    setItem(key, BeeUtils.transform(def.ordinal()));
    return def;
  }

  public int checkInt(String key, int def) {
    String v = getItem(key);
    if (BeeUtils.isDigit(v)) {
      return BeeUtils.toInt(v);
    }

    setItem(key, BeeUtils.transform(def));
    return def;
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

  public List<Property> getAll() {
    List<Property> lst = new ArrayList<Property>();
    int len = length();
    String z;

    for (int i = 0; i < len; i++) {
      z = key(i);
      lst.add(new Property(z, getItem(z)));
    }

    return lst;
  }

  public boolean getBoolean(String key) {
    return BeeUtils.toBoolean(getItem(key));
  }

  public int getInt(String key) {
    return BeeUtils.toInt(getItem(key));
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

  public boolean hasItem(String key) {
    if (BeeUtils.isEmpty(key)) {
      return false;
    }
    return getItem(key) != null;
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

  public void setItem(String key, String value) {
    Assert.notEmpty(key);

    if (BeeUtils.isEmpty(value)) {
      removeItem(key);
    } else if (localStorage) {
      lsSetItem(key, value);
    } else {
      items.put(key, value);
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

  private native void lsSetItem(String key, String value) /*-{
    $wnd.localStorage.setItem(key, value);
  }-*/;

  private boolean validIndex(int index) {
    return index >= 0 && index < length();
  }
}
