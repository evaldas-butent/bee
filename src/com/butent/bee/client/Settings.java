package com.butent.bee.client;

import com.google.common.collect.Maps;
import com.google.gwt.i18n.client.Dictionary;

import com.butent.bee.shared.BeeConst;

import java.util.Map;
import java.util.MissingResourceException;

public class Settings {
  
  private static Dictionary settings = null;
  
  public static Map<String, String> getSettings() {
    Map<String, String> result = Maps.newHashMap();
    if (checkSettings()) {
      for (String key : settings.keySet()) {
        result.put(key, settings.get(key));
      }
    }
    return result;
  }
  
  public static String getVersion() {
    if (checkSettings()) {
      return settings.get("version");
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  private static boolean checkSettings() {
    if (settings == null) {
      readSettings();
    }
    return settings != null;
  }
  
  private static void readSettings() {
    try {
      settings = Dictionary.getDictionary("BeeSettings");
    } catch (MissingResourceException ex) {
      settings = null;
    }
  }
  
  private Settings() {
  }
}
