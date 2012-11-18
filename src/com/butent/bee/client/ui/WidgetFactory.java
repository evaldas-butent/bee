package com.butent.bee.client.ui;

import com.google.common.collect.Maps;

import com.butent.bee.client.Callback;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

public class WidgetFactory {
  
  private static final Map<String, WidgetSupplier> suppliers = Maps.newHashMap();
  
  public static void clear() {
    suppliers.clear();
  }
  
  public static void create(String key, Callback<IdentifiableWidget> callback) {
    if (BeeUtils.isEmpty(key) || callback == null) {
      return;
    }
    
    WidgetSupplier supplier = suppliers.get(BeeUtils.normalize(key));
    if (supplier == null) {
      callback.onFailure("widget supplier not found:", key);
    } else {
      supplier.create(callback);
    }
  }
  
  public static Collection<String> getKeys() {
    return new TreeSet<String>(suppliers.keySet());
  }
  
  public static boolean hasSupplier(String key) {
    if (BeeUtils.isEmpty(key)) {
      return false;
    } else {
      return suppliers.containsKey(BeeUtils.normalize(key));
    }
  }

  public static void registerSupplier(String key, WidgetSupplier supplier) {
    if (!BeeUtils.isEmpty(key) && supplier != null && !hasSupplier(key)) {
      suppliers.put(BeeUtils.normalize(key), supplier);
    }
  }
  
  private WidgetFactory() {
  }
}
