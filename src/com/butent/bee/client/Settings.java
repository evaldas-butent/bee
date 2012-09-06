package com.butent.bee.client;

import com.google.common.collect.Maps;
import com.google.gwt.i18n.client.Dictionary;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;
import java.util.MissingResourceException;

/**
 * Manages a settings array.
 */

public class Settings {

  private static Dictionary settings = null;
  private static boolean initialized = false;

  public static int getActionSensitivityMillis() {
    return getPropertyInt("actionSensitivityMillis");
  }

  public static int getLogCapacity() {
    return getPropertyInt("logCapacity");
  }
  
  public static String getProperty(String name) {
    Assert.notEmpty(name);
    if (checkSettings()) {
      return getQuietly(name);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public static int getPropertyInt(String name) {
    String value = getProperty(name);
    if (BeeUtils.isInt(value)) {
      return BeeUtils.toInt(value);
    } else {
      return BeeConst.UNDEF;
    }
  }

  public static long getPropertyLong(String name) {
    String value = getProperty(name);
    if (BeeUtils.isLong(value)) {
      return BeeUtils.toLong(value);
    } else {
      return BeeConst.UNDEF;
    }
  }

  public static int getProviderMaxPrefetchSteps() {
    return getPropertyInt("providerMaxPrefetchSteps");
  }
  
  public static int getProviderMinPrefetchSteps() {
    return getPropertyInt("providerMinPrefetchSteps");
  }

  public static int getProviderRepeatMillis() {
    return getPropertyInt("providerRepeatMillis");
  }

  public static int getProviderSensitivityMillis() {
    return getPropertyInt("providerSensitivityMillis");
  }
  
  public static Map<String, String> getSettings() {
    Map<String, String> result = Maps.newHashMap();
    if (checkSettings()) {
      for (String key : settings.keySet()) {
        result.put(key, settings.get(key));
      }
    }
    return result;
  }

  public static long getStartMillis() {
    return getPropertyLong("startMillis");
  }

  public static String getVersion() {
    return getProperty("version");
  }

  private static boolean checkSettings() {
    if (!initialized) {
      readSettings();
      initialized = true;
    }
    return settings != null;
  }

  private static String getQuietly(String name) {
    String value;
    try {
      value = settings.get(name);
    } catch (MissingResourceException ex) {
      value = BeeConst.STRING_EMPTY;
    }
    return value;
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
