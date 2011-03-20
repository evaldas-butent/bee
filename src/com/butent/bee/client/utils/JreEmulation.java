package com.butent.bee.client.utils;

import com.butent.bee.shared.utils.ArrayUtils;

public class JreEmulation {

  public static String getSimpleName(Object obj) {
    if (obj == null) {
      return null;
    }
    if (ArrayUtils.isArray(obj) && ArrayUtils.length(obj) > 0) {
      return getSimpleName(ArrayUtils.get(obj, 0)) + "[]";
    }

    String name = obj.getClass().getName();
    int p = name.lastIndexOf(".");

    if (p > 0) {
      return name.substring(p + 1);
    } else {
      return name;
    }
  }
  
  private JreEmulation() {
  }
}
