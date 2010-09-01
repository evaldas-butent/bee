package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeType {
  public static final int TYPE_UNKNOWN = 0;
  public static final int TYPE_BOOLEAN = 1;

  public static final int TYPE_STRING = 2;
  public static final int TYPE_CHAR = 4;
  public static final int TYPE_TEXT = 8;

  public static final int TYPE_NUMBER = 16;
  public static final int TYPE_BYTE = 32;
  public static final int TYPE_SHORT = 64;
  public static final int TYPE_INT = 128;
  public static final int TYPE_LONG = 256;
  public static final int TYPE_DOUBLE = 512;

  public static final int TYPE_DATE = 1024;

  public static final int TYPE_FILE = 2048;
  public static final int TYPE_BLOB = 4096;
  public static final int TYPE_ENUM = 8192;

  public static boolean isValid(int tp) {
    return BeeUtils.inList(tp, TYPE_BOOLEAN, TYPE_STRING, TYPE_CHAR, TYPE_TEXT,
        TYPE_NUMBER, TYPE_BYTE, TYPE_SHORT, TYPE_INT, TYPE_LONG, TYPE_DOUBLE,
        TYPE_DATE, TYPE_FILE, TYPE_BLOB, TYPE_ENUM);
  }

}
