package com.butent.bee.shared.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;

/**
 * Contains methods for processing arrays.
 */
public class ArrayUtils {
  /**
   * Searches the specified collection for a value.
   * 
   * @param value the value to be searched for
   * @param arr the array to search
   * @return true if the value is found, false elsewise
   */
  public static boolean contains(Object value, Object[] arr) {
    return indexOf(value, arr) >= 0;
  }

  /**
   * Searches for a specified CharSeqeuence in the array. Each array element is checked if it
   * contains the specified CharSequence
   * 
   * @param ctxt CharSequence to be searched for
   * @param arr the array to search
   * @return true if the CharSequence is found, elsewise false
   */
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
  
  public static String[] copyOf(String[] original) {
    if (original == null) {
      return null;
    }
    String[] copy = new String[original.length];
    for (int i = 0; i < original.length; i++) {
      copy[i] = original[i];
    }
    return copy;
  }

  /**
   * Copies an array. The specified {@code newLength} determines the length of the array.
   * 
   * @param original the original array
   * @param newLength number of elements to be copied
   * @return a new array
   */
  public static Object[] copyOf(Object[] original, int newLength) {
    Object[] copy = create(newLength);
    if (newLength > 0) {
      System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
    }
    return copy;
  }

  /**
   * Copies an array from specified index to a specified index. If specified {@code to} is longer
   * than the array, all elements from index from are copied.
   * 
   * @param src the array to be copied
   * @param from index from which to start
   * @param to end index
   * @return a new array
   */
  public static Object[] copyOfRange(Object[] src, int from, int to) {
    int dstLen = to - from;
    Object[] dst = create(dstLen);
    if (dstLen > 0) {
      System.arraycopy(src, from, dst, 0, Math.min(src.length - from, dstLen));
    }
    return dst;
  }

  /**
   * Creates a new empty array with specified size.
   * 
   * @param size the size of the new array
   * @return a new empty array.
   */
  public static Object[] create(int size) {
    Assert.nonNegative(size);
    return new Object[size];
  }

  /**
   * Returns an object from specified index from an object array.
   * 
   * @param arr the array returned from
   * @param idx index of the array
   * @return the object from a specified index. If the array is not one of these types: Object,
   *         Boolean, Char, Byte, Short, Integer, Long, Float or Double, it returns null.
   */
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

  /**
   * Returns an array element from the specified index.
   * 
   * @param arr the array returned from
   * @param idx index of the element to be returned
   * @return an array element, or null if index is out of bounds.
   */
  public static <T> T getQuietly(T[] arr, int idx) {
    if (isIndex(arr, idx)) {
      return arr[idx];
    } else {
      return null;
    }
  }

  /**
   * Checks if the value is found within the array.
   * 
   * @param value value to search for
   * @param arr array to be searched from
   * @return -1 if the value is not found, or the index of the found value.
   */
  public static int indexOf(Object value, Object[] arr) {
    int idx = -1;
    int len = length(arr);
    if (len <= 0) {
      return idx;
    }

    for (int i = 0; i < len; i++) {
      if (BeeUtils.equals(value, arr[i])) {
        idx = i;
        break;
      }
    }
    return idx;
  }

  /**
   * Inserts a value to the specified index of an array.
   * 
   * @param source the array to insert to
   * @param index index to insert to
   * @param value value to insert
   * @return an array with the value inserted in specified index
   */
  public static Object[] insert(Object[] source, int index, Object value) {
    Assert.notNull(source);
    Assert.betweenInclusive(index, 0, source.length);
    Object[] result = copyOf(source, source.length + 1);

    result[index] = value;
    for (int i = index + 1; i < source.length + 1; i++) {
      result[i] = source[i - 1];
    }
    return result;
  }

  /**
   * Checks if the specified object is an array.
   * 
   * @param obj object to be checked
   * @return true if object is an array, elsewise false.
   */
  public static boolean isArray(Object obj) {
    return obj instanceof Object[] || isPrimitiveArray(obj);
  }

  /**
   * Checks if a specified index is found in the object.
   * 
   * @param obj the object to check
   * @param idx index to check
   * @return true if the index in the object exists, if not false.
   */
  public static boolean isIndex(Object obj, int idx) {
    if (obj == null || idx < 0) {
      return false;
    } else {
      int n = length(obj);
      return (n > 0 && idx < n);
    }
  }

  /**
   * Checks if the specified object is an instance of any primitive type.
   * 
   * @param obj object to check
   * @return true if the object is a primitive array, otherwise false.
   */
  public static boolean isPrimitiveArray(Object obj) {
    return obj instanceof boolean[] || obj instanceof char[]
        || obj instanceof byte[] || obj instanceof short[]
        || obj instanceof int[] || obj instanceof long[]
        || obj instanceof float[] || obj instanceof double[];
  }

  /**
   * Joins an array with the specified separator. Each array element is joined by the separator.
   * 
   * @param arr array to join
   * @param separator separator to join with
   * @return a new string which contains all array elements joined by the specified separator
   */
  public static String join(Object[] arr, Object separator) {
    return join(arr, separator, -1, -1);
  }

  /**
   * Joins an array with the specified separator from the specified index. Each array element is
   * joined by the separator.
   * 
   * @param arr array to join
   * @param separator separator to join with
   * @param fromIndex the array index to start from
   * @return a new string which contains all array elements joined by the specified separator
   */
  public static String join(Object[] arr, Object separator, int fromIndex) {
    return join(arr, separator, fromIndex, -1);
  }

  /**
   * Joins an array with the specified separator from the specified index to a specified to index.
   * Each array element is joined by the separator.
   * 
   * @param arr array to join
   * @param separator separator to join with
   * @param fromIndex the array index to start from
   * @param toIndex an array index to stop joining
   * @return a new string which contains all array elements joined by the specified separator
   */
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

  /**
   * Gets the length of the specified Object {@code arr}.
   * 
   * @param arr an array to check
   * @return the length of the object if it is one of these types: Object, Boolean, Char, Byte,
   *         Short, Integer, Long, Float, Double. 0 if its none of these types.
   */
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

  /**
   * Removes an element in the specified {@code index} from the array.
   * 
   * @param source array to remove from
   * @param index the element in the index to remove
   * @return a new array with the value from the specified index removed.
   */
  public static Object[] remove(Object[] source, int index) {
    Assert.isIndex(source, index);
    Object[] result;

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

  /**
   * Copies the specified range of the {@code source} array into a new array.
   * Implements JavaScript array.slice method. Null-safe.
   * 
   * @param source array to slice 
   * @param start specifies where to start the selection (The first element has an index of 0).
   *        Negative value selects from the end of an array.
   * @return a new array containing all elements from the start position to the end of
   *         the original array.
   */
  public static String[] slice(String[] source, int start) {
    if (source == null) {
      return null;
    }
    return slice(source, start, source.length);
  }
  
  /**
   * Copies the specified range of the {@code source} array into a new array.
   * Implements JavaScript array.slice method. Null-safe.
   * 
   * @param source array to slice 
   * @param start specifies where to start the selection (The first element has an index of 0).
   *        Negative value selects from the end of an array.
   * @param end specifies where to end the selection.
   *        Negative value selects from the end of an array.    
   * @return a new array containing the specified range from the original array.
   */
  public static String[] slice(String[] source, int start, int end) {
    if (source == null) {
      return null;
    }
    int srcLen = source.length;
    if (srcLen <= 0 || start >= srcLen || end <= -srcLen) {
      return BeeConst.EMPTY_STRING_ARRAY;
    }
    
    int p1 = (start >= 0) ? start : Math.max(srcLen + start, 0);
    int p2 = (end >= 0) ? Math.min(srcLen, end) : Math.max(srcLen + end, 0);
    if (p1 >= p2) {
      return BeeConst.EMPTY_STRING_ARRAY;
    }
    
    int len = p2 - p1;
    String[] arr = new String[len];
    for (int i = 0; i < len; i++) {
      arr[i] = source[p1 + i];
    }
    return arr;
  }

  /**
   * Transforms an array recursively using the specified separators. Each recursive level uses the
   * next separator. If there are no separators defined, it uses the default ", " separator.
   * 
   * @param arr the array to transform
   * @param sep separator list
   * @return a String joined from the specified array.
   */
  public static String transform(Object arr, Object... sep) {
    if (BeeUtils.isEmpty(arr)) {
      return BeeConst.STRING_EMPTY;
    }
    int cSep = (sep == null) ? 0 : sep.length;
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
      sb.append(BeeUtils.transformGeneric(el, nextSep));
    }
    return sb.toString();
  }

  private ArrayUtils() {
  }
}
