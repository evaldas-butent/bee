package com.butent.bee.shared.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;

public class ArrayUtils {

  public static <T> boolean contains(T value, T[] arr) {
    return indexOf(value, arr) >= 0;
  }

  public static boolean context(CharSequence ctxt, String[] arr) {
    boolean ok = false;
    if (BeeUtils.isEmpty(ctxt) || BeeUtils.isEmpty(arr)) {
      return ok;
    }

    for (String el : arr) {
      if (BeeUtils.context(ctxt, el)) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  public static <T> T[] copyOf(T[] original, int newLength) {
    T[] copy = create(newLength);
    if (newLength > 0) {
      System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
    }
    return copy;
  }

  public static <T> T[] copyOfRange(T[] src, int from, int to) {
    int dstLen = to - from;
    T[] dst = create(dstLen);
    if (dstLen > 0) {
      System.arraycopy(src, from, dst, 0, Math.min(src.length - from, dstLen));
    }
    return dst;
  }

  // TODO Array.newInstance
  @SuppressWarnings("unchecked")
  public static <T> T[] create(int size) {
    Assert.nonNegative(size);
    return (T[]) new Object[size];
  }

  public static Object get(Object arr, int idx) {
    if (arr instanceof Object[]) {
      return ((Object[]) arr)[idx];
    } else if (arr instanceof boolean[]) {
      return ((boolean[]) arr)[idx];
    } else if (arr instanceof char[]) {
      return ((char[]) arr)[idx];
    } else if (arr instanceof byte[]) {
      return ((byte[]) arr)[idx];
    } else if (arr instanceof short[]) {
      return ((short[]) arr)[idx];
    } else if (arr instanceof int[]) {
      return ((int[]) arr)[idx];
    } else if (arr instanceof long[]) {
      return ((long[]) arr)[idx];
    } else if (arr instanceof float[]) {
      return ((float[]) arr)[idx];
    } else if (arr instanceof double[]) {
      return ((double[]) arr)[idx];
    } else {
      return null;
    }
  }

  public static <T> T getQuietly(T[] arr, int idx) {
    if (isIndex(arr, idx)) {
      return arr[idx];
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> int indexOf(T value, T[] arr) {
    int idx = -1;
    int len = length(arr);
    if (len <= 0) {
      return idx;
    }

    for (int i = 0; i < len; i++) {
      if (value == arr[i]) {
        idx = i;
        break;
      }

      if (value instanceof Comparable<?> && arr[i] != null
          && ((Comparable<T>) value).compareTo(arr[i]) == 0) {
        idx = i;
        break;
      }
    }
    return idx;
  }

  public static <T> T[] insert(T[] source, int index, T value) {
    Assert.notNull(source);
    Assert.betweenInclusive(index, 0, source.length);
    T[] result = copyOf(source, source.length + 1);

    result[index] = value;
    for (int i = index + 1; i < source.length + 1; i++) {
      result[i] = source[i - 1];
    }
    return result;
  }

  public static boolean isArray(Object obj) {
    return obj instanceof Object[] || isPrimitiveArray(obj);
  }

  public static boolean isIndex(Object obj, int idx) {
    if (obj == null || idx < 0) {
      return false;
    } else {
      int n = length(obj);
      return (n > 0 && idx < n);
    }
  }

  public static boolean isPrimitiveArray(Object obj) {
    return obj instanceof boolean[] || obj instanceof char[]
        || obj instanceof byte[] || obj instanceof short[]
        || obj instanceof int[] || obj instanceof long[]
        || obj instanceof float[] || obj instanceof double[];
  }

  public static String join(Object[] arr, Object separator) {
    return join(arr, separator, -1, -1);
  }

  public static String join(Object[] arr, Object separator, int fromIndex) {
    return join(arr, separator, fromIndex, -1);
  }

  public static String join(Object[] arr, Object separator, int fromIndex, int toIndex) {
    int len = length(arr);
    int fr = (fromIndex > 0) ? fromIndex : 0;
    int to = (toIndex >= 0 && toIndex <= len) ? toIndex : len;

    if (fr >= to) {
      return BeeConst.STRING_EMPTY;
    }

    String sep = BeeUtils.normSep(separator);
    StringBuilder sb = new StringBuilder();

    for (int i = fr; i < to; i++) {
      if (sb.length() > 0) {
        sb.append(sep);
      }
      sb.append(BeeUtils.transform(arr[i]));
    }
    return sb.toString();
  }

  public static int length(Object arr) {
    int len;

    if (arr instanceof Object[]) {
      len = ((Object[]) arr).length;
    } else if (arr instanceof boolean[]) {
      len = ((boolean[]) arr).length;
    } else if (arr instanceof char[]) {
      len = ((char[]) arr).length;
    } else if (arr instanceof byte[]) {
      len = ((byte[]) arr).length;
    } else if (arr instanceof short[]) {
      len = ((short[]) arr).length;
    } else if (arr instanceof int[]) {
      len = ((int[]) arr).length;
    } else if (arr instanceof long[]) {
      len = ((long[]) arr).length;
    } else if (arr instanceof float[]) {
      len = ((float[]) arr).length;
    } else if (arr instanceof double[]) {
      len = ((double[]) arr).length;
    } else {
      len = 0;
    }
    return len;
  }

  public static <T> T[] remove(T[] source, int index) {
    Assert.isIndex(source, index);
    T[] result;

    if (index == 0) {
      result = copyOfRange(source, 1, source.length);
    } else {
      result = copyOf(source, source.length - 1);
      for (int i = index; i < source.length - 1; i++) {
        result[i] = source[i + 1];
      }
    }
    return result;
  }

  public static String transform(Object arr, Object... sep) {
    if (BeeUtils.isEmpty(arr)) {
      return BeeConst.STRING_EMPTY;
    }
    int cSep = sep.length;
    String z = cSep > 0 ? BeeUtils.normSep(sep[0]) : BeeConst.DEFAULT_LIST_SEPARATOR;

    StringBuilder sb = new StringBuilder();
    Object el;
    Object[] nextSep;

    if (cSep > 1) {
      nextSep = new Object[cSep - 1];
      for (int i = 0; i < cSep - 1; i++) {
        nextSep[i] = sep[i + 1];
      }
    } else {
      nextSep = new String[]{z};
    }

    int r = length(arr);

    for (int i = 0; i < r; i++) {
      el = get(arr, i);
      if (i > 0) {
        sb.append(z);
      }
      sb.append(BeeUtils.transform(el, nextSep));
    }
    return sb.toString();
  }
}
