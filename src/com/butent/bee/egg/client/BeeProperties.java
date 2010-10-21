package com.butent.bee.egg.client;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;

public class BeeProperties implements BeeModule {
  private static Map<String, String> properties = new HashMap<String, String>();

  public static boolean getBooleanProperty(String key) {
    return BeeUtils.toBoolean(getProperty(key));
  }

  public static int getIntProperty(String key) {
    String s = getProperty(key);

    if (BeeUtils.isEmpty(s)) {
      return 0;
    } else {
      return Integer.parseInt(s);
    }
  }

  public static String getProperty(String key) {
    if (BeeUtils.isEmpty(key)) {
      return BeeConst.STRING_EMPTY;
    } else {
      return properties.get(key);
    }
  }

  public static String setProperty(String key, Object value) {
    if (BeeUtils.isEmpty(key)) {
      return null;
    }

    String v = (value == null ? BeeConst.STRING_EMPTY
        : BeeUtils.transform(value));
    properties.put(key, v);

    return v;
  }

  public void end() {
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

  public void start() {
  }

}
