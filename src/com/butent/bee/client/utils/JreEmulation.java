package com.butent.bee.client.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public class JreEmulation {
  public static String[] copyOfRange(String[] src, int from, int to) {
    int dstLen = to - from;
    Assert.nonNegative(dstLen);
    
    String[] dst = new String[dstLen];
    if (dstLen > 0) {
      System.arraycopy(src, from, dst, 0, Math.min(src.length - from, dstLen));
    }
    return dst;
  }

  public static String getSimpleName(Object obj) {
    if (obj == null) {
      return null;
    }
    if (BeeUtils.isArray(obj) && BeeUtils.arrayLength(obj) > 0) {
      return getSimpleName(BeeUtils.arrayGet(obj, 0)) + "[]";
    }

    String name = obj.getClass().getName();
    int p = name.lastIndexOf(".");

    if (p > 0) {
      return name.substring(p + 1);
    } else {
      return name;
    }
  }
}
