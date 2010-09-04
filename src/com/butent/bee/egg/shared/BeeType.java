package com.butent.bee.egg.shared;

import java.util.Date;

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
  public static final int TYPE_FLOAT = 512;
  public static final int TYPE_DOUBLE = 1024;

  public static final int TYPE_DATE = 2048;

  public static final int TYPE_FILE = 4096;
  public static final int TYPE_BLOB = 8192;
  public static final int TYPE_ENUM = 16384;

  public static final int TYPE_NULL = 32768;

  public static boolean isValid(int tp) {
    return BeeUtils.inList(tp, TYPE_BOOLEAN, TYPE_STRING, TYPE_CHAR, TYPE_TEXT,
        TYPE_NUMBER, TYPE_BYTE, TYPE_SHORT, TYPE_INT, TYPE_LONG, TYPE_FLOAT,
        TYPE_DOUBLE, TYPE_DATE, TYPE_FILE, TYPE_BLOB, TYPE_ENUM);
  }

  public static int getType(Object x) {
    if (x == null) {
      return TYPE_NULL;
    } else if (x instanceof Boolean) {
      return TYPE_BOOLEAN;
    } else if (x instanceof String) {
      return TYPE_STRING;
    } else if (x instanceof Character) {
      return TYPE_CHAR;
    } else if (x instanceof CharSequence) {
      return TYPE_TEXT;
    } else if (x instanceof Byte) {
      return TYPE_BYTE;
    } else if (x instanceof Short) {
      return TYPE_SHORT;
    } else if (x instanceof Integer) {
      return TYPE_INT;
    } else if (x instanceof Long) {
      return TYPE_LONG;
    } else if (x instanceof Float) {
      return TYPE_FLOAT;
    } else if (x instanceof Double) {
      return TYPE_DOUBLE;
    } else if (x instanceof Number) {
      return TYPE_NUMBER;
    } else if (x instanceof BeeNumber) {
      return TYPE_NUMBER;
    } else if (x instanceof Date) {
      return TYPE_DATE;
    } else if (x instanceof BeeDate) {
      return TYPE_DATE;
    } else {
      return TYPE_UNKNOWN;
    }
  }

}
