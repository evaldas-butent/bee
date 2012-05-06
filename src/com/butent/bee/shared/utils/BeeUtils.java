package com.butent.bee.shared.utils;

import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.HasLength;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.time.TimeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains base methods for developement.
 */
public class BeeUtils {

  public static <T> boolean addNotEmpty(Collection<T> col, T item) {
    if (isEmpty(item)) {
      return false;
    } else {
      Assert.notNull(col).add(item);
      return true;
    }
  }

  public static <T> boolean addNotNull(Collection<T> col, T item) {
    if (item == null) {
      return false;
    } else {
      Assert.notNull(col).add(item);
      return true;
    }
  }
  
  /**
   * Checks if all specified objects are empty.
   * 
   * @param obj objects to check
   * @return true if all of the specified objects are empty. Returns false if any of these objects
   *         aren't empty.
   */
  public static boolean allEmpty(Object... obj) {
    if (obj == null) {
      return true;
    }
    Assert.parameterCount(obj.length, 1);
    boolean ok = true;

    for (Object z : obj) {
      if (!isEmpty(z)) {
        ok = false;
        break;
      }
    }
    return ok;
  }

  /**
   * Checks if all specified objects are <b>not empty</b>.
   * 
   * @param obj objects to check
   * @return true if all of the specified objects are not empty. Returns false if any of these
   *         objects are empty.
   */
  public static boolean allNotEmpty(Object... obj) {
    Assert.notNull(obj);
    Assert.parameterCount(obj.length, 1);
    boolean ok = true;

    for (Object z : obj) {
      if (isEmpty(z)) {
        ok = false;
        break;
      }
    }
    return ok;
  }

  public static boolean anyEmpty(Object... obj) {
    Assert.notNull(obj);
    Assert.parameterCount(obj.length, 1);
    boolean ok = false;

    for (Object z : obj) {
      if (isEmpty(z)) {
        ok = true;
        break;
      }
    }
    return ok;
  }
  
  /**
   * Appends specified list's {@code iterable} elements to a specified StringBuilder {@code bld}.
   * 
   * @param bld a builder used for appending
   * @param iterable a list to append
   * @param sep separator used for appending the list. List elements are separated by this
   *          separator.
   * @return a string builder with the list elements appended.
   */
  public static StringBuilder append(StringBuilder bld, Iterable<?> iterable, Object sep) {
    if (iterable != null) {
      String z = normSep(sep);
      Iterator<?> it = iterable.iterator();

      while (it.hasNext()) {
        bld.append(it.next());
        if (it.hasNext()) {
          bld.append(z);
        }
      }
    }
    return bld;
  }

  /**
   * Appends array elements to a specified StringBuilder.
   * 
   * @param bld a builder used for appending
   * @param arr array to append
   * @param sep separator used for appending the array. Elements are separated by this separator.
   * @return a string builder with the array elements appended.
   */
  public static StringBuilder append(StringBuilder bld, Object[] arr, Object sep) {
    if (arr != null && arr.length > 0) {
      String z = normSep(sep);
      bld.append(arr[0]);

      for (int i = 1; i < arr.length; i++) {
        bld.append(z);
        bld.append(arr[i]);
      }
    }
    return bld;
  }
  
  /**
   * Checks if the specified value {@code x} is between values {@code min} and {@code max}. Note:
   * {@code min} value is inclusive, {@code max} value - exclusive.
   * 
   * @param x value to check
   * @param min the minimum value
   * @param max the maximum value
   * @return true if x is between {@code min} (inclusively) and {@code max} (exclusively), else
   *         false.
   */
  public static boolean betweenExclusive(int x, int min, int max) {
    return x >= min && x < max;
  }

  /**
   * Checks if the specified value {@code x} is between values {@code min} and {@code max}
   * inclusively.
   * 
   * @param x value to check
   * @param min the minimum value
   * @param max the maximum value
   * @return true if {@code x} is between {@code min} and {@code max} inclusively, else false.
   */
  public static boolean betweenInclusive(int x, int min, int max) {
    return x >= min && x <= max;
  }

  /**
   * Surrounds the Object value {@code x} in brackets.
   * 
   * @param x Object to put in brackets.
   * @return a String representation of the Object surrounded by brackets.
   */
  public static String bracket(Object x) {
    String s = transform(x);

    if (s.isEmpty()) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeConst.STRING_LEFT_BRACKET + s + BeeConst.STRING_RIGHT_BRACKET;
    }
  }

  /**
   * Gets the value between {@code min} and {@code max}.
   * 
   * @param x a value to return in the specified limits
   * @param min the minimum possible value
   * @param max the maximum possible value
   * @return {@code x} if the value is between {@code min} and {@code max}, is the value is less
   *         than {@code min} it returns {@code min}, if greater than {@code max} it returns
   *         {@code max}.
   */
  public static double clamp(double x, double min, double max) {
    if (!isDouble(x)) {
      if (isDouble(min)) {
        return min;
      }
      if (isDouble(max)) {
        return max;
      }
      return x;
    }

    double z;
    if (isDouble(min) && isDouble(max)) {
      z = Math.min(min, max);
      if (x < z) {
        return z;
      }
      z = Math.max(min, max);
      if (x > z) {
        return z;
      }
      return x;
    }

    if (isDouble(min)) {
      return Math.max(x, min);
    }
    if (isDouble(max)) {
      return Math.min(x, max);
    }
    return x;
  }

  /**
   * Gets the value between {@code min} and {@code max}.
   * 
   * @param x a value to return in the specified limits
   * @param min the minimum possible value
   * @param max the maximum possible value
   * @return x if the value is between {@code min} and {@code max}, if the value is less than
   *         {@code min} it returns {@code min}, if greater than {@code max} it returns {@code max}.
   */
  public static int clamp(int x, int min, int max) {
    int z = Math.min(min, max);
    if (x < z) {
      return z;
    }
    z = Math.max(min, max);
    if (x > z) {
      return z;
    }
    return x;
  }

  /**
   * A string in the specified position is appended with "..." and the left string after the index
   * {@code n} is replaced with a progress indicator.
   * <p>
   * E.g {@code clip("This is a sentence", 6)} result is {@code  "This i...[6/18]"}
   * </p>
   * 
   * @param s a String to modify
   * @param n an index in the String where to modify
   * @return returns a String with a progress indicator after the specified {@code n} index
   */
  public static String clip(String s, int n) {
    Assert.isPositive(n);
    if (isEmpty(s)) {
      return BeeConst.STRING_EMPTY;
    }
    if (s.length() <= n) {
      return s;
    }

    int len = s.trim().length();

    if (len <= n) {
      return s.trim();
    } else {
      return s.substring(0, n).trim() + BeeConst.ELLIPSIS + bracket(progress(n, len));
    }
  }

  /**
   * Compares {@code x1} and {@code x2}. Both values must implement the Comparable interface. This
   * method allows to compare values even if one or both of the specified values are {@code null}.
   * 
   * @param x1 first value to compare
   * @param x2 second value to compare
   * @return 0 if objects are equal, -1 if {@code x1 < x2}, and 1 if @code {x1 > x2}.
   */
  @SuppressWarnings("unchecked")
  public static <T> int compare(Comparable<T> x1, Comparable<T> x2) {
    if (x1 == null) {
      if (x2 == null) {
        return BeeConst.COMPARE_EQUAL;
      } else {
        return BeeConst.COMPARE_LESS;
      }
    } else if (x2 == null) {
      return BeeConst.COMPARE_MORE;
    } else {
      return x1.compareTo((T) x2);
    }
  }

  /**
   * Compares the two specified Double values.
   * 
   * @param x1 the first Double value to be compared
   * @param x2 the second Double value to be compared
   * @return 0 if values are equal, -1 if {@code x1 < x2}, and 1 if @code {x1 > x2}.
   */
  public static int compare(double x1, double x2) {
    if (x1 == x2 || ((Double) x1).equals(x2)) {
      return BeeConst.COMPARE_EQUAL;
    } else if (x1 < x2) {
      return BeeConst.COMPARE_LESS;
    } else {
      return BeeConst.COMPARE_MORE;
    }
  }

  /**
   * Compares the two specified Integer values.
   * 
   * @param x1 the first Integer value to be compared
   * @param x2 the second Integer value to be compared
   * @return 0 if values are equal, -1 if {@code x1 < x2}, and 1 if {@code x1 > x2}.
   */
  public static int compare(int x1, int x2) {
    if (x1 == x2) {
      return BeeConst.COMPARE_EQUAL;
    } else if (x1 < x2) {
      return BeeConst.COMPARE_LESS;
    } else {
      return BeeConst.COMPARE_MORE;
    }
  }

  public static int compare(long x1, long x2) {
    if (x1 == x2) {
      return BeeConst.COMPARE_EQUAL;
    } else if (x1 < x2) {
      return BeeConst.COMPARE_LESS;
    } else {
      return BeeConst.COMPARE_MORE;
    }
  }

  /**
   * Compares objects {@code x1} and {@code x2}. This method allows to compare values even if one or
   * both of the specified values are {@code null}.
   * 
   * @param x1 first value to compare
   * @param x2 second value to compare
   * @return 0 if objects are equal, -1 if {@code x1 < x2}, and 1 if {@code x1 > x2}.
   */
  public static int compare(Object x1, Object x2) {
    if (x1 == null) {
      if (x2 == null) {
        return BeeConst.COMPARE_EQUAL;
      } else {
        return BeeConst.COMPARE_LESS;
      }
    } else if (x2 == null) {
      return BeeConst.COMPARE_MORE;
    } else if (x1 == x2 || x1.equals(x2)) {
      return BeeConst.COMPARE_EQUAL;
    } else {
      return x1.toString().compareTo(x2.toString());
    }
  }

  /**
   * Compares the two specified String values.
   * 
   * @param s1 the first String value to be compared
   * @param s2 the second String value to be compared
   * @return 0 if values are equal, -1 if {@code x1 < x2}, and 1 if {@code x1 > x2}.
   */
  public static int compare(String s1, String s2) {
    if (isEmpty(s1)) {
      if (isEmpty(s2)) {
        return BeeConst.COMPARE_EQUAL;
      } else {
        return BeeConst.COMPARE_LESS;
      }
    } else if (isEmpty(s2)) {
      return BeeConst.COMPARE_MORE;
    } else {
      return s1.compareTo(s2);
    }
  }

  /**
   * Compares two normalized String values.
   * 
   * @param s1 first normalized String value
   * @param s2 second normalized String value
   * @return 0 if values are equal, -1 if {@code s1 < s2}, and 1 if {@code s1 > s2}.
   */
  public static int compareNormalized(String s1, String s2) {
    return compare(normalize(s1), normalize(s2));
  }

  /**
   * Concats specified Objects. The first argument is the separator for the rest of the arguments.
   * 
   * @param x Objects to concatenate
   * @return a String concatenized using the fist Object as a separator, an empty string if no or
   *         only single Object is specified.
   */
  public static String concat(Object... x) {
    int c = (x == null) ? 0 : x.length;
    if (c <= 1) {
      return BeeConst.STRING_EMPTY;
    }

    StringBuilder s = new StringBuilder();
    String sep = normSep(x[0]);

    for (int i = 1; i < c; i++) {
      if (!isEmpty(x[i], EnumSet.of(BeeType.NUMBER))) {
        if (s.length() > 0) {
          s.append(sep);
        }
        s.append(transformGeneric(x[i], sep));
      }
    }
    return s.toString();
  }

  public static <T> boolean contains(Collection<? extends T> col, T item) {
    if (col == null) {
      return false;
    } else {
      return col.contains(item);
    }
  }

  public static boolean contains(String s, char ch) {
    if (s == null) {
      return false;
    } else {
      return s.indexOf(ch) >= 0;
    }
  }

  /**
   * Checks is there are equal elements in the Collections.
   * 
   * @param c1 first Collection's elements to be compared
   * @param c2 second Collection's elements to be compared
   * @return true if one of the collections contain at least one equal element from the other
   *         collection, otherwise false.
   */
  public static <T> boolean containsAny(Collection<T> c1, Collection<T> c2) {
    boolean ok = false;

    int n1 = length(c1);
    if (n1 <= 0) {
      return ok;
    }
    int n2 = length(c2);
    if (n2 <= 0) {
      return ok;
    }

    if (n1 <= n2) {
      for (T el : c1) {
        if (c2.contains(el)) {
          ok = true;
          break;
        }
      }
    } else {
      for (T el : c2) {
        if (c1.contains(el)) {
          ok = true;
          break;
        }
      }
    }
    return ok;
  }

  /**
   * Checks if the CharSequence {@code src} contains only of the specified characters.
   * 
   * @param src source CharSequence to be checked
   * @param ch characters to check for
   * @return true if the src contains only the specified characters, false if src contains any
   *         different characters.
   */
  public static boolean containsOnly(CharSequence src, char ch) {
    if (src == null) {
      return false;
    }
    int len = src.length();
    if (len <= 0) {
      return false;
    }

    boolean ok = true;
    for (int i = 0; i < len; i++) {
      if (src.charAt(i) != ch) {
        ok = false;
        break;
      }
    }
    return ok;
  }

  public static boolean containsSame(Collection<String> col, String s) {
    if (isEmpty(col)) {
      return false;
    }

    for (String entry : col) {
      if (same(entry, s)) {
        return true;
      }
    }
    return false;
  }

  public static boolean containsSame(String src, String ctxt) {
    if (isEmpty(src) || isEmpty(ctxt)) {
      return false;
    } else {
      return src.trim().toLowerCase().contains(ctxt.trim().toLowerCase());
    }
  }

  public static boolean containsWhitespace(CharSequence cs) {
    if (cs == null) {
      return false;
    }
    for (int i = 0; i < cs.length(); i++) {
      if (isWhitespace(cs.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if {@code src} contains {@code ctxt}. Both values are compared after transforming to
   * lower case.
   * 
   * @param ctxt value to check for
   * @param src source value to check in
   * @return true if {@code src} contains {@code ctxt}, otherwise false.
   */
  public static boolean context(CharSequence ctxt, CharSequence src) {
    if (ctxt == null || src == null || ctxt.length() == 0 || src.length() == 0) {
      return false;
    } else {
      return src.toString().toLowerCase().contains(ctxt.toString().toLowerCase());
    }
  }

  /**
   * Checks if {@code ctxt} contains any of the elements from {@code src}. Compared values are
   * transformed to lower case.
   * 
   * @param ctxt value to check for
   * @param src list of elements to compare to
   * @return true if any of {@code src} elements contain {@code ctxt}, otherwise false.
   */
  public static boolean context(CharSequence ctxt, Collection<? extends CharSequence> src) {
    boolean ok = false;
    if (isEmpty(ctxt)) {
      return ok;
    }

    for (CharSequence el : src) {
      if (context(ctxt, el)) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  /**
   * Checks if {@code src} contains any of the elements from {@code ctxt}. Compared values are
   * transformed to lower case.
   * 
   * @param ctxt list of elements to be compared
   * @param src source value to check in
   * @return true if {@code src} contains any of the elements from {@code ctxt}, false if
   *         {@code src} is empty or if it doesn't contain any of the elements.
   */
  public static boolean context(Collection<? extends CharSequence> ctxt, CharSequence src) {
    boolean ok = false;
    if (isEmpty(src)) {
      return ok;
    }

    for (CharSequence el : ctxt) {
      if (context(el, src)) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  /**
   * Counts the appearances of the specified character {@code ch} in a CharSequence.
   * 
   * @param src source to count in
   * @param ch characters to count
   * @return a number of the specified character appearances in the CharSequence.
   */
  public static int count(CharSequence src, char ch) {
    int cnt = 0;
    if (src == null) {
      return cnt;
    }

    for (int i = 0; i < src.length(); i++) {
      if (src.charAt(i) == ch) {
        cnt++;
      }
    }
    return cnt;
  }

  /**
   * Deletes a part of a String from specified {@code start} to {@code end}.
   * 
   * @param src source String to delete from
   * @param start position to start deleting from
   * @param end position to end deleting
   * @return a String without the deleted part. empty String - if {@code src} is {@code null} or
   *         empty, {@code src} - if start and end is wrong to given String
   */
  public static String delete(String src, int start, int end) {
    if (src == null) {
      return BeeConst.STRING_EMPTY;
    }
    int len = src.length();
    if (len <= 0) {
      return BeeConst.STRING_EMPTY;
    }
    if (start >= len || start >= end || end <= 0) {
      return src;
    }

    if (start <= 0 && end >= len) {
      return BeeConst.STRING_EMPTY;
    }
    if (start <= 0) {
      return src.substring(end);
    }
    if (end >= len) {
      return src.substring(0, start);
    }
    return src.substring(0, start) + src.substring(end);
  }

  /**
   * Returns the elapsed time in seconds.
   * 
   * @param start the start time
   * @return the elapsed time in seconds from the specified start in brackets.
   */
  public static String elapsedSeconds(long start) {
    return bracket(toSeconds(System.currentTimeMillis() - start));
  }

  /**
   * Checks if Object {@code x} and Object {@code y} are equal.
   * 
   * @param x fist Object to compare
   * @param y second Object to compare
   * @return true if Objects are equal, false if Objects differ.
   */
  public static boolean equals(Object x, Object y) {
    if (x == null) {
      return y == null;
    } else {
      return x.equals(y);
    }
  }

  /**
   * Trims both Strings and then compares them.
   * 
   * @param s1 the String to compare
   * @param s2 the String to compare
   * @return true if trimmed Strings are equal, false if Strings differ or any of them are empty or
   *         {@code null}.
   */
  public static boolean equalsTrim(String s1, String s2) {
    if (s1 == null) {
      return isEmpty(s2);
    } else if (s2 == null) {
      return isEmpty(s1);
    } else {
      return s1.trim().equals(s2.trim());
    }
  }

  public static boolean equalsTrimRight(String s1, String s2) {
    if (s1 == null) {
      return isEmpty(s2);
    } else if (s2 == null) {
      return isEmpty(s1);
    } else {
      return trimRight(s1).equals(trimRight(s2));
    }
  }

  /**
   * Raises the value {@code z} to the power of 10.
   * 
   * @param z value to raise to the power of 10
   * @return of value of the specified argument raised to the power of 10.
   */
  public static int exp10(int z) {
    Assert.nonNegative(z);
    Double x = Math.pow(10, z);
    Assert.isTrue(x < Integer.MAX_VALUE);
    return x.intValue();
  }

  /**
   * Filters Object {@code x} through BeeType set. Gets the type of Object {@code x} and checks if
   * any of the set types contain it.
   * 
   * @param x the Object to filter
   * @param types the set of elements to compare to
   * @return true when types contain the type of Object, otherwise false
   */
  public static boolean filterType(Object x, Set<BeeType> types) {
    Assert.notEmpty(types);

    Set<BeeType> tp = Sets.newHashSet();

    if (x == null) {
      tp.add(BeeType.NULL);
    } else if (x instanceof Boolean) {
      tp.add(BeeType.BOOLEAN);
    } else if (instanceOfStringType(x)) {
      tp.add(BeeType.STRING);
    } else if (x instanceof Character) {
      tp.add(BeeType.CHAR);
    } else if (x instanceof Number) {
      tp.add(BeeType.NUMBER);
      if (instanceOfIntegerType(x)) {
        tp.add(BeeType.INT);
      }
      if (instanceOfFloatingPoint(x)) {
        tp.add(BeeType.FLOAT);
        tp.add(BeeType.DOUBLE);
      }
    } else if (TimeUtils.isDateOrDateTime(x)) {
      tp.add(BeeType.DATE);
    } else {
      tp.add(BeeType.UNKNOWN);
    }

    return containsAny(types, tp);
  }

  /**
   * Looks for a fitting starting position.
   * 
   * @param start the start index
   * @param len the length
   * @param end the end index
   * @return the new starting position
   */
  public static int fitStart(int start, int len, int end) {
    if (start + len <= end) {
      return start;
    } else {
      return end - len;
    }
  }

  /**
   * Looks for a fitting starting position.
   * 
   * @param start the start index
   * @param len the length
   * @param end end index
   * @param min the minimum starting position
   * @return the new starting position
   */
  public static int fitStart(int start, int len, int end, int min) {
    return max(fitStart(start, len, end), min);
  }

  /**
   * Converts a String representation of Hex symbol to a corresponding Hex symbol.
   * <p>
   * <b>Note: the specified String must be in double bytes.</b> E.g "005D" is "]".
   * <p>
   * 
   * @param s the string to convert
   * @return returns a corresponding Hex symbol for the specified String input, {@code null} if the
   *         specified String format is wrong
   */
  public static char[] fromHex(String s) {
    if (!isHexString(s)) {
      return null;
    }

    if (s.length() <= 4) {
      return new char[] {(char) Integer.parseInt(s, 16)};
    } else {
      int z = s.length() % 4;
      int n;

      if (z == 0) {
        n = s.length() / 4;
      } else {
        n = (s.length() - z) / 4 + 1;
      }

      char[] arr = new char[n];
      String u;

      for (int i = 0; i < n; i++) {
        if (z == 0) {
          u = s.substring(i * 4, (i + 1) * 4);
        } else if (i == 0) {
          u = s.substring(0, z);
        } else {
          u = s.substring(z + (i - 1) * 4, z + i * 4);
        }

        arr[i] = (char) Integer.parseInt(u, 16);
      }
      return arr;
    }
  }

  /**
   * If any {@code src} Collection element contains {@code ctxt} (case is ignored), than that
   * element is added to the new Collection and returned after all elements from {@code src}
   * Collection are covered.
   * 
   * @param ctxt context to search for
   * @param src source list to search from
   * @return a new list with elements that contain {@code ctxt} in {@code src} collection.
   */
  public static <T extends CharSequence> List<T> getContext(T ctxt, Collection<T> src) {
    List<T> lst = new ArrayList<T>();
    if (isEmpty(ctxt)) {
      return lst;
    }

    for (T el : src) {
      if (context(ctxt, el)) {
        lst.add(el);
      }
    }
    return lst;
  }

  /**
   * Gets the key of the value from the specified Map when the Mmap contains the value.
   * 
   * @param map the Map to search for the value
   * @param value the value to search for
   * @return a key when the value is found, {@code null} if there is no such value
   */
  public static <K, V> K getKey(Map<K, V> map, V value) {
    K key = null;
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (equals(entry.getValue(), value)) {
        key = entry.getKey();
        break;
      }
    }
    return key;
  }

  /**
   * Gets a prefix from a String, where separator sets that the prefix will end at the first
   * occurrence of the separator.
   * 
   * @param src the source to get a prefix from
   * @param sep the separator.
   * @return a String, where the length is determined by the first occurrence of the separator.
   */
  public static String getPrefix(String src, char sep) {
    if (isEmpty(src)) {
      return BeeConst.STRING_EMPTY;
    }

    int p = src.indexOf(sep);

    if (p > 0) {
      return src.substring(0, p).trim();
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  /**
   * Separates a String with separator value. Returns a String that goes before the separator.
   * 
   * @param src source to get the prefix from
   * @param sep the separator
   * @return a prefix value that goes before the separator.
   */
  public static String getPrefix(String src, String sep) {
    if (isEmpty(src) || length(sep) <= 0) {
      return BeeConst.STRING_EMPTY;
    }

    int p = src.indexOf(sep);

    if (p > 0) {
      return src.substring(0, p).trim();
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  /**
   * Returns a value from the list in the specified index.
   * 
   * @param lst the list to return from
   * @param idx the element which to return by index
   * @return return an element in the specified position.
   */
  public static <T> T getQuietly(List<? extends T> lst, int idx) {
    if (isIndex(lst, idx)) {
      return lst.get(idx);
    } else {
      return null;
    }
  }

  public static String getSame(Collection<String> col, String s) {
    if (isEmpty(col)) {
      return null;
    }

    for (String entry : col) {
      if (same(entry, s)) {
        return entry;
      }
    }
    return null;
  }

  /**
   * Separates a string with separator value. Returns a string that goes after the separator.
   * 
   * @param src source string to get suffix from
   * @param sep separator
   * @return a suffix value that goes after the separator.
   */
  public static String getSuffix(String src, char sep) {
    if (isEmpty(src)) {
      return BeeConst.STRING_EMPTY;
    }

    int p = src.lastIndexOf(sep);

    if (p >= 0 && p < src.length() - 1) {
      return src.substring(p + 1).trim();
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  /**
   * Separates a string with separator value. Returns a string that goes after the separator.
   * 
   * @param src source string to get suffix from
   * @param sep separator
   * @return a suffix value that goes after the separator.
   */
  public static String getSuffix(String src, String sep) {
    if (isEmpty(src) || length(sep) <= 0) {
      return BeeConst.STRING_EMPTY;
    }

    int p = src.lastIndexOf(sep);

    if (p >= 0 && p < src.length() - sep.length()) {
      return src.substring(p + sep.length()).trim();
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public static boolean hasLength(CharSequence cs, int min) {
    if (cs == null) {
      return false;
    }
    return cs.length() >= min;
  }

  /**
   * @param x Object to check
   * @param def Objects {@code x} default value to set
   * @return the String from the Object if the Object is an instance of String, else returns a
   *         default String value {@code def}.
   */
  public static String ifString(Object x, String def) {
    if (x instanceof String && !isEmpty(x)) {
      return (String) x;
    } else {
      return def;
    }
  }

  /**
   * Extended if sentence. Basic syntax : if {@code obj[i] == true} the method jumps to
   * {@code obj[i+1]} else jumps to {@code obj[i+2]}
   * 
   * @param obj Objects to check
   * @return {@code obj[i+1]} if condition {@code obj[i]} is true, or {@code obj[i+2]} if condition
   *         is false.
   */
  @SuppressWarnings("unchecked")
  public static <T> T iif(Object... obj) {
    Assert.notNull(obj);
    int n = obj.length;
    Assert.parameterCount(n, 3);

    T z = ((n % 2 == 1) ? (T) obj[n - 1] : null);

    for (int i = 0; i < n - 1; i += 2) {
      if (!isEmpty(obj[i])) {
        z = (T) obj[i + 1];
        break;
      }
    }
    return z;
  }

  /**
   * Transforms the Object to a String and increments the value by 1.
   * 
   * @param obj object to increment
   * @return a String representation of the incremented Object value.
   */
  public static String increment(Object obj) {
    return increment(transform(obj));
  }

  /**
   * Transforms the String to Integer and increments it by 1.
   * 
   * @param s a string to increment
   * @return a String representation of the incremented String value.
   */
  public static String increment(String s) {
    return Integer.toString(toInt(s) + 1);
  }

  public static int indexOf(List<String> lst, String s) {
    if (isEmpty(lst)) {
      return BeeConst.UNDEF;
    }

    for (int i = 0; i < lst.size(); i++) {
      if (same(lst.get(i), s)) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  public static boolean inList(int x, int... lst) {
    Assert.notNull(lst);
    boolean ok = false;

    for (int i = 0; i < lst.length; i++) {
      if (x == lst[i]) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  /**
   * Checks if value {@code x} is in {@code lst}.
   * 
   * @param x the value to check for
   * @param lst variables to check in
   * @return true if {@code x} was found in these variables, false if they did not contain the value
   *         {@code x}.
   */
  public static boolean inList(String x, String... lst) {
    Assert.notNull(x);
    Assert.notNull(lst);
    boolean ok = false;

    for (int i = 0; i < lst.length; i++) {
      if (lst[i] == null) {
        continue;
      }
      if (x.trim().equals(lst[i].trim())) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  /**
   * Checks if the String value {@code x} equals any value of {@code lst}. Note: case is ignored.
   * 
   * @param x a string to check
   * @param lst a list to check in
   * @return true if value x string is found, otherwise false
   */
  public static boolean inListIgnoreCase(String x, String... lst) {
    Assert.notEmpty(x);
    Assert.notNull(lst);
    boolean ok = false;

    for (int i = 0; i < lst.length; i++) {
      if (lst[i] == null) {
        continue;
      }
      if (x.equalsIgnoreCase(lst[i])) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  /**
   * Checks if {@code x} is found in any of the {@code lst} Strings.
   * 
   * @param x value to search for
   * @param lst all Strings to search
   * @return true if {@code x} is found in {@code lst}, false otherwise.
   */
  public static boolean inListSame(String x, String... lst) {
    Assert.notEmpty(x);
    Assert.notNull(lst);
    boolean ok = false;

    String z = x.trim().toLowerCase();

    for (int i = 0; i < lst.length; i++) {
      if (lst[i] == null) {
        continue;
      }
      if (z.equalsIgnoreCase(lst[i].trim())) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  /**
   * Inserts a character to a specified position in the String.
   * 
   * @param src the string to be inserted to
   * @param pos position to insert to
   * @param c character to insert
   * @return a String with the character inserted in the specified position
   */
  public static String insert(String src, int pos, char c) {
    Assert.notNull(src);
    Assert.nonNegative(pos);
    Assert.isTrue(pos <= src.length());

    return new StringBuilder(src).insert(pos, c).toString();
  }

  /**
   * Inserts a CharSequence to a specified position in the specified String.
   * 
   * @param src the String to be inserted to
   * @param pos position to insert to
   * @param cs CharSequence to insert
   * @return a String with the CharSequence inserted in the specified String.
   */
  public static String insert(String src, int pos, CharSequence cs) {
    Assert.notNull(src);
    Assert.nonNegative(pos);
    Assert.isTrue(pos <= src.length());
    Assert.hasLength(cs);

    return new StringBuilder(src).insert(pos, cs).toString();
  }

  /**
   * Checks if Object {@code x} is an instance of a floating point(Double or Float).
   * 
   * @param x the Object to check
   * @return true if the Object is an instance of a floating point, false otherwise
   */
  public static boolean instanceOfFloatingPoint(Object x) {
    if (x == null) {
      return false;
    } else {
      return (x instanceof Float || x instanceof Double);
    }
  }

  /**
   * Checks if Object {@code x} is an instance of an integer type(Byte, Short, Integer, Long,
   * BigInteger).
   * 
   * @param x Object to check
   * @return true if the Object is an instance of an integer type, false otherwise
   */
  public static boolean instanceOfIntegerType(Object x) {
    if (x == null) {
      return false;
    } else {
      return (x instanceof Byte || x instanceof Short || x instanceof Integer
          || x instanceof Long || x instanceof BigInteger);
    }
  }

  /**
   * Checks if Object {@code x} is an instance of a String(String, StringBuilder, StringBuffer).
   * 
   * @param x Object to check
   * @return true if the Object is an instance of String type, false otherwise
   */
  public static boolean instanceOfStringType(Object x) {
    if (x == null) {
      return false;
    } else {
      return (x instanceof String || x instanceof StringBuilder || x instanceof StringBuffer);
    }
  }

  public static boolean isBetween(Double d, Double min, boolean minInclusive,
      Double max, boolean maxInclusive) {
    if (!isDouble(d)) {
      return false;
    }

    boolean ok = true;
    if (isDouble(min)) {
      ok = minInclusive ? (d >= min) : (d > min);
    }
    if (ok && isDouble(max)) {
      ok = maxInclusive ? (d <= max) : (d > max);
    }
    return ok;
  }

  /**
   * Checks if {@code x} is a Boolean value (0 or 1).
   * 
   * @param x the number to check
   * @return true if integer equals 1, false if integer is {@code <= 0 or > 1}.
   */
  public static boolean isBoolean(int x) {
    return x == BeeConst.INT_TRUE || x == BeeConst.INT_FALSE;
  }

  /**
   * Checks if {@code s} is a Boolean value.
   * 
   * @param s a String to check
   * @return true if s equals "true", "false", "yes", "no", false otherwise.
   */
  public static boolean isBoolean(String s) {
    if (s == null) {
      return false;
    } else {
      return BeeConst.isTrue(s) || BeeConst.isFalse(s);
    }
  }

  public static boolean isDecimal(String s) {
    if (isEmpty(s)) {
      return false;
    }
    BigDecimal d;

    try {
      d = new BigDecimal(s.trim());
    } catch (NumberFormatException ex) {
      d = null;
    }
    return d != null;
  }

  public static boolean isDelimited(CharSequence cs, char delimiter) {
    return isDelimited(cs, delimiter, delimiter);
  }

  public static boolean isDelimited(CharSequence cs, char start, char end) {
    if (cs == null || cs.length() < 2) {
      return false;
    } else {
      return cs.charAt(0) == start && cs.charAt(cs.length() - 1) == end;
    }
  }

  /**
   * Checks if a character {@code c} is a digit.
   * 
   * @param c character to check
   * @return true if the character is {@code >= 0 and <= 9};
   */
  public static boolean isDigit(char c) {
    return c >= BeeConst.CHAR_ZERO && c <= BeeConst.CHAR_NINE;
  }

  /**
   * Checks if a CharacterSequence {@code d} is a digit.
   * 
   * @param s CharSequence to check
   * @return true if all characters in the sequence are digits, false if sequence is {@code null} or
   *         empty or contains at least one non-digit character.
   */
  public static boolean isDigit(CharSequence s) {
    if (s == null) {
      return false;
    }

    int len = s.length();
    if (len < 1) {
      return false;
    }
    boolean ok = true;

    for (int i = 0; i < len; i++) {
      if (!isDigit(s.charAt(i))) {
        ok = false;
        break;
      }
    }
    return ok;
  }

  /**
   * Checks if the value {@code x} is Double.
   * 
   * @param x a Double value to check
   * @return true if x is a number and is not infinite, otherwise false
   */
  public static boolean isDouble(Double x) {
    return x != null && !Double.isNaN(x) && !Double.isInfinite(x);
  }

  /**
   * Checks if value {@code s} can be cast to a Double value.
   * 
   * @param s a string value to check
   * @return true if {@code s} value can be cast to Double, false otherwise.
   */
  public static boolean isDouble(String s) {
    if (isEmpty(s)) {
      return false;
    }
    boolean ok;

    try {
      Double.parseDouble(s);
      ok = true;
    } catch (NumberFormatException ex) {
      ok = false;
    }
    return ok;
  }

  public static boolean isDouble(String s, Double min, boolean minInclusive) {
    return isDouble(s, min, minInclusive, null, false);
  }

  public static boolean isDouble(String s, Double min, boolean minInclusive,
      Double max, boolean maxInclusive) {
    if (isEmpty(s)) {
      return false;
    }

    boolean ok;
    try {
      double d = Double.parseDouble(s);
      ok = isBetween(d, min, minInclusive, max, maxInclusive);
    } catch (NumberFormatException ex) {
      ok = false;
    }
    return ok;
  }

  /**
   * Checks if an Object is empty.
   * 
   * @param x an Object to check
   * @return true if the Object is empty, false otherwise.
   */
  public static boolean isEmpty(Object x) {
    boolean ok;

    if (x == null) {
      ok = true;
    } else if (x instanceof String) {
      ok = ((String) x).isEmpty() || ((String) x).trim().isEmpty();
    } else if (x instanceof CharSequence) {
      ok = ((CharSequence) x).length() == 0
          || ((CharSequence) x).toString().trim().isEmpty();
    } else if (x instanceof Number) {
      ok = isZero(x);
    } else if (x instanceof Boolean) {
      ok = !(Boolean) x;
    } else if (x instanceof Collection) {
      ok = ((Collection<?>) x).isEmpty();
    } else if (x instanceof Map) {
      ok = ((Map<?, ?>) x).isEmpty();
    } else if (ArrayUtils.isArray(x)) {
      ok = ArrayUtils.length(x) <= 0;
    } else if (x instanceof Enumeration) {
      ok = !((Enumeration<?>) x).hasMoreElements();
    } else if (x instanceof HasLength) {
      ok = ((HasLength) x).getLength() <= 0;
    } else {
      ok = false;
    }
    return ok;
  }

  /**
   * Checks if an Object {@code x} is empty, and if it not a BeeType.
   * 
   * @param orType type Set to filter.
   * @return true if an Object is empty, false if Object contains a type in the {@code orType} or is
   *         not empty.
   */
  public static boolean isEmpty(Object x, Set<BeeType> orType) {
    if (filterType(x, orType)) {
      return false;
    } else {
      return isEmpty(x);
    }
  }

  public static boolean isFalse(Boolean b) {
    return Boolean.FALSE.equals(b);
  }

  /**
   * Checks if the specified character contains only Hex digits.
   * 
   * @param c the value to check
   * @return true if the value contains only Hex digits, false otherwise.
   */
  public static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F');
  }

  /**
   * Checks if the specified String contains only Hex digits.
   * 
   * @param s the value to check
   * @return true if the String contains only Hex digits,false otherwise.
   */
  public static boolean isHexString(String s) {
    if (isEmpty(s)) {
      return false;
    }
    boolean ok = true;

    for (int i = 0; i < s.length(); i++) {
      if (!isHexDigit(s.charAt(i))) {
        ok = false;
        break;
      }
    }
    return ok;
  }

  /**
   * Checks if an Object has the specified index.
   * 
   * @param obj an Object to check
   * @param idx Objects index to check
   * @return true if such index in the object exists, false otherwise.
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
   * Checks if a Double value can be transformed to an Integer value.
   * 
   * @param x double value to transform
   * @return true if x is more than the integer's minimum value and less than the maximum, false
   *         otherwise.
   */
  public static boolean isInt(double x) {
    return isDouble(x) && x > Integer.MIN_VALUE && x < Integer.MAX_VALUE;
  }

  /**
   * Checks if a String value can be transformed to an Integer value.
   * 
   * @param s a string value to transform
   * @return true if the string has a correct number format, otherwise false.
   */
  public static boolean isInt(String s) {
    if (isEmpty(s)) {
      return false;
    }
    boolean ok;

    try {
      Integer.parseInt(s.trim());
      ok = true;
    } catch (NumberFormatException ex) {
      ok = false;
    }
    return ok;
  }

  public static <T> boolean isLeq(Comparable<T> x1, Comparable<T> x2) {
    return compare(x1, x2) <= 0;
  }

  public static <T> boolean isLess(Comparable<T> x1, Comparable<T> x2) {
    return compare(x1, x2) < 0;
  }

  /**
   * Checks if a Double value can be transformed to a Long value.
   * 
   * @param x the value to check
   * @return true if x value range is in long range, false otherwise.
   */
  public static boolean isLong(double x) {
    return isDouble(x) && x > Long.MIN_VALUE && x < Long.MAX_VALUE;
  }

  public static boolean isLong(String s) {
    if (isEmpty(s)) {
      return false;
    }
    boolean ok;

    try {
      Long.parseLong(s.trim());
      ok = true;
    } catch (NumberFormatException ex) {
      ok = false;
    }
    return ok;
  }

  public static <T> boolean isMeq(Comparable<T> x1, Comparable<T> x2) {
    return compare(x1, x2) >= 0;
  }

  public static <T> boolean isMore(Comparable<T> x1, Comparable<T> x2) {
    return compare(x1, x2) > 0;
  }

  public static boolean isNonNegative(Double d) {
    if (isDouble(d)) {
      return Double.compare(d, BeeConst.DOUBLE_ZERO) >= 0;
    } else {
      return false;
    }
  }

  public static boolean isNonNegativeDouble(String s) {
    return isDouble(s, BeeConst.DOUBLE_ZERO, true);
  }

  /**
   * @param clazz the class to check for Enum constants
   * @param idx the index to check
   * @return true if an Enum with the specified index {@code idx} exists, otherwise false.
   */
  public static boolean isOrdinal(Class<?> clazz, Integer idx) {
    if (clazz == null || !clazz.isEnum() || idx == null || idx < 0) {
      return false;
    }
    return idx < ArrayUtils.length(clazz.getEnumConstants());
  }

  public static boolean isPositive(Double d) {
    if (isDouble(d)) {
      return Double.compare(d, BeeConst.DOUBLE_ZERO) > 0;
    } else {
      return false;
    }
  }

  public static boolean isPositive(Integer x) {
    return (x == null) ? false : x > 0;
  }

  public static boolean isPositiveDouble(String s) {
    return isDouble(s, BeeConst.DOUBLE_ZERO, false);
  }

  public static boolean isPositiveInt(String s) {
    return isInt(s) && toInt(s) > 0;
  }

  /**
   * Checks if the first character in a CharSequence is a prefix.
   * 
   * @param src sequence to ceck
   * @param pfx a prefix to check for
   * @return true if the first character equals {@code pfx}, otherwise false.
   */
  public static boolean isPrefix(CharSequence src, char pfx) {
    if (src == null || src.length() <= 0) {
      return false;
    }
    return src.charAt(0) == pfx;
  }

  public static boolean isPrefix(String src, String pfx) {
    if (src == null || pfx == null) {
      return false;
    }
    int srcLen = src.length();
    int pfxLen = pfx.length();
    if (pfxLen <= 0 || srcLen <= pfxLen) {
      return false;
    }
    return same(left(src, pfxLen), pfx);
  }

  /**
   * Checks if the specified character is a prefix or a suffix.
   * 
   * @param src a sequence to check in
   * @param ch a suffix of prefix to search for
   * @return true if the first or the last character equals {@code pfx}, false if the sequence
   *         contains only one symbol and is suffix or prefix, and if the sequence does not contain
   *         nor suffix nor prefix
   */
  public static boolean isPrefixOrSuffix(CharSequence src, char ch) {
    return (isPrefix(src, ch) || isSuffix(src, ch)) && !containsOnly(src, ch);
  }

  /**
   * Checks if the last character in a CharSequence is a suffix.
   * 
   * @param src sequence to check
   * @param sfx a suffix to check for
   * @return true if the last character equals {@code sfx}, otherwise false.
   */
  public static boolean isSuffix(CharSequence src, char sfx) {
    if (src == null) {
      return false;
    }
    int len = src.length();
    if (len <= 0) {
      return false;
    }
    return src.charAt(len - 1) == sfx;
  }

  public static boolean isSuffix(String src, String sfx) {
    if (src == null || sfx == null) {
      return false;
    }
    int srcLen = src.length();
    int sfxLen = sfx.length();
    if (sfxLen <= 0 || srcLen <= sfxLen) {
      return false;
    }
    return same(right(src, sfxLen), sfx);
  }

  /**
   * Checks if the specified value is true.
   * 
   * @param b value to check
   * @return true if the Object is a Boolean.TRUE, otherwise false.
   */
  public static boolean isTrue(Boolean b) {
    if (b == null) {
      return false;
    }
    return b.booleanValue();
  }

  public static boolean isWhitespace(char ch) {
    return ch <= BeeConst.CHAR_SPACE || ch == BeeConst.CHAR_NBSP;
  }

  /**
   * Checks if a BigDecimal value is zero.
   * 
   * @param x value to check
   * @return true if {@code x == 0}, otherwise false.
   */
  public static boolean isZero(BigDecimal x) {
    return x == BigDecimal.ZERO;
  }

  /**
   * Checks if a BigInteger value is zero.
   * 
   * @param x value to check
   * @return true if it is 0, otherwise false.
   */
  public static boolean isZero(BigInteger x) {
    return x == BigInteger.ZERO;
  }

  /**
   * Checks if Byte value is zero.
   * 
   * @param x value to check
   * @return true if {@code x ==0} , false otherwise.
   */
  public static boolean isZero(byte x) {
    return x == 0;
  }

  /**
   * Checks if a Double value is zero.
   * 
   * @param x value to check
   * @return true if x == 0.0, otherwise false.
   */
  public static boolean isZero(double x) {
    return x == Double.valueOf(BeeConst.DOUBLE_ZERO);
  }

  /**
   * Checks if a Float value is zero.
   * 
   * @param x value to check
   * @return true if {@code x == 0.0}, otherwise false
   */
  public static boolean isZero(float x) {
    return x == Float.valueOf(BeeConst.FLOAT_ZERO);
  }

  /**
   * Checks if an Integer value is zero.
   * 
   * @param x value to check
   * @return true if {@code x==0}, otherwise false.
   */
  public static boolean isZero(int x) {
    return x == 0;
  }

  /**
   * Checks if a Long value is zero.
   * 
   * @param x value to check
   * @return true if {@code x == 0L}, otherwise false.
   */
  public static boolean isZero(long x) {
    return x == 0L;
  }

  /**
   * Checks if an Object is zero.
   * 
   * @param x value to check
   * @return true if and Object is zero, otherwise false.
   */
  public static boolean isZero(Object x) {
    if (x instanceof Number) {
      return ((Number) x).doubleValue() == Double.valueOf(BeeConst.DOUBLE_ZERO);
    } else {
      return false;
    }
  }

  /**
   * Checks if a Short value is zero.
   * 
   * @param x value to check
   * @return true if x is zero, otherwise false.
   */
  public static boolean isZero(short x) {
    return x == 0;
  }

  /**
   * Joins specified lists to a one list and returns it.
   * 
   * @param src all lists to be joined
   * @return a new list containing all elements from the {@code src} lists.
   */
  public static <T> List<T> join(List<? extends T>... src) {
    Assert.notNull(src);
    int n = src.length;
    Assert.parameterCount(n, 2);

    List<T> dst = new ArrayList<T>();

    for (List<? extends T> lst : src) {
      if (lst != null) {
        dst.addAll(lst);
      }
    }
    return dst;
  }

  /**
   * Cuts the string from the beginning to a specified index.
   * 
   * @param s string to cut
   * @param n index to cut to
   * @return a String with a cut part out of the String.
   */
  public static String left(String s, int n) {
    if (s == null) {
      return null;
    } else if (n <= 0) {
      return BeeConst.STRING_EMPTY;
    } else if (s.length() <= n) {
      return s;
    } else {
      return s.substring(0, n);
    }
  }

  /**
   * Gets the length of an Object(CharSequence, Character, Collection, Map, HasLenght, Array).
   * 
   * @param x the value to get length from
   * @return the length of the supported Object. If an Object is not supported it returns 0.
   */
  public static int length(Object x) {
    int len;

    if (x == null) {
      len = 0;
    } else if (x instanceof CharSequence) {
      len = ((CharSequence) x).length();
    } else if (x instanceof Character) {
      len = 1;
    } else if (x instanceof Collection) {
      len = ((Collection<?>) x).size();
    } else if (x instanceof Map) {
      len = ((Map<?, ?>) x).size();
    } else if (x instanceof HasLength) {
      len = ((HasLength) x).getLength();
    } else if (ArrayUtils.isArray(x)) {
      len = ArrayUtils.length(x);
    } else {
      len = 0;
    }
    return len;
  }

  /**
   * Gets an Object with the greatest value.
   * 
   * @param x the objects
   * @return the greatest value of all of the Objects.
   */
  public static <T extends Comparable<T>> T max(T... x) {
    Assert.notNull(x);
    int n = x.length;
    Assert.parameterCount(n, 2);
    T z = x[0];

    for (int i = 1; i < n; i++) {
      if (x[i].compareTo(z) > 0) {
        z = x[i];
      }
    }
    return z;
  }

  /**
   * Gets an Object with the smallest value.
   * 
   * @param x the objects
   * @return the smallest value of all of the Objects.
   */
  public static <T extends Comparable<T>> T min(T... x) {
    Assert.notNull(x);
    int n = x.length;
    Assert.parameterCount(n, 2);
    T z = x[0];

    for (int i = 1; i < n; i++) {
      if (x[i].compareTo(z) < 0) {
        z = x[i];
      }
    }
    return z;
  }

  public static String nextString(String value) {
    String expression = trim(value);
    String pattern = "^(.*?)(\\d*?)$";
    String prefix = expression.replaceFirst(pattern, "$1");
    String suffix = expression.replaceFirst(pattern, "$2");

    if (isEmpty(suffix)) {
      suffix = "0";
    }
    int l = suffix.length();
    suffix = BeeUtils.transform(Long.parseLong(suffix) + 1);

    return prefix + padLeft(suffix, l, '0');
  }

  public static double normalize(double x, double min, double max) {
    double z;

    if (min > max) {
      z = 1.0 - normalize(x, max, min);
    } else if (min == max) {
      z = 0;
    } else if (x <= min) {
      z = 0;
    } else if (x >= max) {
      z = 1;
    } else {
      z = (x - min) / (max - min);
    }
    return z;
  }

  /**
   * Normalizes the String to lower case.
   * 
   * @param s string to be normalized
   * @return a normalized string
   */
  public static String normalize(String s) {
    if (s == null) {
      return BeeConst.STRING_EMPTY;
    }
    return s.trim().toLowerCase();
  }

  /**
   * Returns an Object as a separator. If an Object is a number it's returned spaces quantity equal
   * to the numeric value. String, Char and CharSequence type Objects are transformed to String
   * values and returned.
   * 
   * @param x the object to create a separator from
   * @return a String separator
   */
  public static String normSep(Object x) {
    String sep;

    if (x instanceof String && length(x) > 0) {
      sep = (String) x;
    } else if (x instanceof Number) {
      sep = space(((Number) x).intValue());
    } else if (x instanceof Character) {
      sep = new String(new char[] {(Character) x});
    } else if (x instanceof CharSequence && length(x) > 0) {
      sep = ((CharSequence) x).toString();
    } else {
      sep = BeeConst.DEFAULT_LIST_SEPARATOR;
    }
    return sep;
  }

  /**
   * Returns an Object as a separator. If an Object is a number it's returned spaces quantity equal
   * to the numeric value. String, Char and CharSequence type Objects are transformed to String
   * values and returned. If an Object is none of these types it return the default Object
   * {@code def} as a separator.
   * 
   * @param x the Object to create a separator from
   * @param def the default Object value
   * @return a String separator
   */
  public static String normSep(Object x, Object def) {
    String sep;

    if (x instanceof CharSequence && length(x) > 0 
        || x instanceof Integer && isPositive((Integer) x) || x instanceof Character) {
      sep = normSep(x);
    } else {
      sep = normSep(def);
    }

    return sep;
  }

  /**
   * Gets the first object which is not {@code null}. At least 2 Objects must be specified.
   * 
   * @param obj objects to check
   * @return the first not null object in specified objects. Returns null if no such object is
   *         found.
   */
  public static <T> T nvl(T... obj) {
    Assert.notNull(obj);
    Assert.parameterCount(obj.length, 2);
    T z = null;

    for (T x : obj) {
      if (x != null) {
        z = x;
        break;
      }
    }
    return z;
  }

  public static <T> void overwrite(Collection<T> target, Collection<T> source) {
    Assert.notNull(target);
    if (!target.isEmpty()) {
      target.clear();
    }
    
    if (source != null) {
      target.addAll(source);
    }
  }

  /**
   * Pads left with a specified symbol {@code z}, for length of {@code n - s.length}. If the String
   * is longer than {@code n}, it returns the original String.
   * 
   * @param s a String to pad left
   * @param n specifies the length
   * @param z character to use for padding
   * @return returns a left padded String with a specified {@code z} character.
   */
  public static String padLeft(String s, int n, char z) {
    if (s == null) {
      return null;
    } else if (n <= 0) {
      return BeeConst.STRING_EMPTY;
    } else if (s.length() >= n) {
      return s;
    } else {
      return replicate(z, n - s.length()) + s;
    }
  }

  /**
   * Transforms an Object to a String and parenthesizes it.
   * 
   * @param x object to parenthesize
   * @return a parenthesized String from both sides.
   */
  public static String parenthesize(Object x) {
    String s = transform(x);

    if (s.isEmpty()) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeConst.STRING_LEFT_PARENTHESIS + s + BeeConst.STRING_RIGHT_PARENTHESIS;
    }
  }

  public static <T> T peek(Iterable<T> container) {
    if (container == null) {
      return null;
    }
    for (T element : container) {
      return element;
    }
    return null;
  }

  /**
   * Compares {@code x1} and {@code x2}.
   * 
   * @param x1 the Object to compare
   * @param x2 the Object to compare
   * @return 0 if both objects are equal, 1 if {@code x1} > {@code x2} and -1 if {@code x1} <
   *         {@code x2}.
   */
  public static int precompare(Object x1, Object x2) {
    if (x1 == x2) {
      return BeeConst.COMPARE_EQUAL;
    }
    if (x1 == null) {
      if (x2 == null) {
        return BeeConst.COMPARE_EQUAL;
      } else {
        return BeeConst.COMPARE_LESS;
      }
    }
    if (x2 == null) {
      return BeeConst.COMPARE_MORE;
    }
    return BeeConst.COMPARE_UNKNOWN;
  }

  /**
   * Shows how much of the progress is done, separated by the default progress separator.
   * <p>
   * E.g. the current position is 5 of a total 10. Method returns "5/10".
   * </p>
   * 
   * @param pos current position
   * @param tot total count of positions
   * @return a String representation of the progress.
   */
  public static String progress(int pos, int tot) {
    return Integer.toString(pos) + BeeConst.DEFAULT_PROGRESS_SEPARATOR + Integer.toString(tot);
  }

  public static String progress(int pos, int tot, String message) {
    return concat(BeeConst.STRING_SPACE, progress(pos, tot), message);
  }

  /**
   * Separates the String {@code s} to an array with a specified separator. Capitalizes each array
   * element's first letter and converts other to lower-case.
   * <p>
   * E.g the separator is {@code "."}: {@code "this.IS.a.string"} is formed to
   * {@code "This Is A String"}.
   * 
   * @param s the String to form
   * @param separators used for separating {@code s}
   * @return a new formed String
   */
  public static String proper(String s, Object separators) {
    if (isEmpty(s)) {
      return BeeConst.STRING_EMPTY;
    }

    int len = s.trim().length();
    if (len <= 0) {
      return BeeConst.STRING_EMPTY;
    }
    if (len == 1) {
      return s.trim().toUpperCase();
    }

    if (separators == null) {
      return s.trim().substring(0, 1).toUpperCase() + s.trim().substring(1).toLowerCase();
    }

    String[] arr = split(s.trim(), separators);
    StringBuilder z = new StringBuilder();

    for (String x : arr) {
      if (z.length() > 0) {
        z.append(BeeConst.CHAR_SPACE);
      }
      z.append(proper(x, null));
    }
    return z.toString();
  }

  public static String quote(String s) {
    if (s == null) {
      return BeeConst.STRING_QUOT + BeeConst.STRING_QUOT;
    } else {
      return BeeConst.STRING_QUOT + s.trim() + BeeConst.STRING_QUOT;
    }
  }

  public static double randomDouble(double min, double max) {
    Assert.isTrue(max > min);
    return min + Math.random() * (max - min);
  }

  /**
   * Generates a random Integer value in range of the specified {@code min} and {@code max} values.
   * 
   * @param min the minimum value
   * @param max the maximum value
   * @return a generated Integer value
   */
  public static int randomInt(int min, int max) {
    Assert.isTrue(max > min);
    Double z = Math.floor(Math.random() * (max - min));
    return min + z.intValue();
  }

  /**
   * Generates a random Long value in range of the specified {@code min} and {@code max} values.
   * 
   * @param min the minimum value
   * @param max the maximum value
   * @return a generated value
   */
  public static long randomLong(long min, long max) {
    Assert.isTrue(max > min);
    Double z = Math.floor(Math.random() * (max - min));
    return min + z.longValue();
  }

  /**
   * Generates a random String with a specified length from the given characters.
   * 
   * @param len length to generate.
   * @param characters characters to use for generating a new string
   * @return a generated random String
   */
  public static String randomString(int len, CharSequence characters) {
    Assert.isPositive(len);
    Assert.hasLength(characters);
    int cnt = characters.length();
    if (cnt == 1) {
      return replicate(characters.charAt(0), len);
    }

    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(characters.charAt(randomInt(0, cnt)));
    }
    return sb.toString();
  }

  /**
   * Generates a random String with a specified length from the given characters.
   * 
   * @param minLen the minimum length of the string
   * @param maxLen the maximum length of the string
   * @param minChar the minimum character to use for generation
   * @param maxChar the maximum character to use for generation
   * @return a generated random String
   */
  public static String randomString(int minLen, int maxLen, char minChar, char maxChar) {
    int len;
    int x = (minLen > 0) ? minLen : 1;
    int y = (maxLen >= x) ? maxLen : x + 20;

    if (x == y) {
      len = minLen;
    } else {
      len = randomInt(x, y + 1);
    }

    x = minChar;
    y = (maxChar >= x) ? maxChar : Math.min(x + 30, Character.MAX_VALUE);

    if (x == y) {
      return replicate(minChar, len);
    }

    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append((char) randomInt(x, y + 1));
    }
    return sb.toString();
  }

  public static String remove(String str, char ch) {
    if (str == null || str.indexOf(ch) < 0) {
      return str;
    }

    char[] arr = str.toCharArray();
    int cnt = 0;
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] != ch) {
        if (cnt < i) {
          arr[cnt] = arr[i];
        }
        cnt++;
      }
    }

    if (cnt > 0) {
      return new String(arr, 0, cnt);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  /**
   * Removes the specified prefix from a String.
   * 
   * @param str a value to remove the prefix from
   * @param pfx prefix to remove
   * @return a value with the prefix removed
   */
  public static String removePrefix(String str, char pfx) {
    if (isPrefix(str, pfx)) {
      return removePrefix(str.substring(1), pfx);
    }
    return str;
  }

  public static String removePrefix(String str, String pfx) {
    if (isPrefix(str, pfx)) {
      return str.substring(pfx.length());
    }
    return str;
  }

  /**
   * Removes specified prefix and suffix from a String.
   * 
   * @param str a value to remove the prefix and suffix from
   * @param ch a prefix and suffix to remove
   * @return a String value with prefix and suffix removed.
   */
  public static String removePrefixAndSuffix(String str, char ch) {
    return removeSuffix(removePrefix(str, ch), ch);
  }

  /**
   * Removes the specified suffix from a String.
   * 
   * @param str a value to remove the prefix from
   * @param sfx prefix to remove
   * @return a value with the suffix removed
   */
  public static String removeSuffix(String str, char sfx) {
    if (isSuffix(str, sfx)) {
      return removeSuffix(str.substring(0, str.length() - 1), sfx);
    }
    return str;
  }

  public static String removeSuffix(String str, String sfx) {
    if (isSuffix(str, sfx)) {
      return left(str, str.length() - sfx.length());
    }
    return str;
  }

  /**
   * Removes all trailing zeros.
   * 
   * @param str a value to remove trailing zeros from
   * @return a value with trailing zeros removed
   */
  public static String removeTrailingZeros(String str) {
    if (str == null) {
      return null;
    }
    if (str.length() <= 2) {
      return str;
    }
    if (str.charAt(str.length() - 1) != BeeConst.CHAR_ZERO) {
      return str;
    }

    int p = str.indexOf(BeeConst.CHAR_POINT);
    if (p < 1) {
      return str;
    }
    if (!isDigit(str.substring(p + 1))) {
      return str;
    }

    int idx = str.length() - 1;
    while (str.charAt(idx - 1) == BeeConst.CHAR_ZERO) {
      idx--;
    }
    if (idx == p + 1) {
      idx--;
    }
    return str.substring(0, idx);
  }

  /**
   * Removes a corresponding Map entry from the Map where value {@code V} equals the Map's element
   * value.
   * 
   * @param map a Map to remove the Map entry from
   * @param value the value to search from
   * @return a Map with the value removed.
   */
  public static <V> int removeValue(Map<?, V> map, V value) {
    int c = 0;
    if (length(map) <= 0) {
      return c;
    }

    for (Iterator<V> it = map.values().iterator(); it.hasNext();) {
      if (equals(it.next(), value)) {
        it.remove();
        c++;
      }
    }
    return c;
  }

  /**
   * Replaces the specified part of the {@code src} with a {@code c} value.
   * 
   * @param src the source String to replace
   * @param start the start position of the replacement
   * @param end the end position of the replacement
   * @param c a String to replace with
   * @return a new String after the replacement.
   */
  public static String replace(String src, int start, int end, char c) {
    return replace(src, start, end, String.valueOf(c));
  }

  /**
   * Replaces the specified part of the {@code src} with a {@code str} value.
   * 
   * @param src the source string to replace
   * @param start the start position of the replacement
   * @param end the end position of the replacement
   * @param str a string to replace with
   * @return a new String after the replacement.
   */
  public static String replace(String src, int start, int end, String str) {
    Assert.isIndex(src, start);
    Assert.isIndex(src, end - 1);
    Assert.notNull(str);

    return new StringBuilder(src).replace(start, end, str).toString();
  }

  /**
   * Replaces all occurrences of {@code search} with {@code replacement}.
   * 
   * @param text the source text
   * @param search search phrase to replace
   * @param replacement a replacement for the search phrase
   * @return a String with replaced phrases.
   */
  public static String replace(String text, String search, String replacement) {
    return replace(text, search, replacement, -1);
  }

  /**
   * Replaces the specified number of occurrences of {@code search} with {@code replacement}.
   * 
   * @param text the source text
   * @param search search phrase to replace
   * @param replacement a replacement for the search phrase
   * @param max the number of occurrences to replace
   * @return a String with replaced phrases.
   */
  public static String replace(String text, String search, String replacement, int max) {
    if (isEmpty(text) || isEmpty(search) || replacement == null || max == 0) {
      return text;
    }
    int start = 0;
    int end = text.indexOf(search, start);
    if (end < 0) {
      return text;
    }

    int len = search.length();
    StringBuilder sb = new StringBuilder();
    int cnt = max;

    while (end >= 0) {
      sb.append(text.substring(start, end)).append(replacement);
      start = end + len;
      if (--cnt == 0) {
        break;
      }
      end = text.indexOf(search, start);
    }
    sb.append(text.substring(start));

    return sb.toString();
  }

  /**
   * Fills a String with value {@code z} for the length of {@code n}.
   * 
   * @param z the value to fill with
   * @param n length to fill
   * @return a filled String
   */
  public static String replicate(char z, int n) {
    Assert.isPositive(n);

    char[] arr = new char[n];
    Arrays.fill(arr, z);
    return new String(arr);
  }

  public static double rescale(double x, double frMin, double frMax, double toMin, double toMax) {
    return scaleNormalizedToRange(normalize(x, frMin, frMax), toMin, toMax);
  }

  public static String right(String s, int n) {
    if (s == null) {
      return null;
    } else if (n <= 0) {
      return BeeConst.STRING_EMPTY;
    } else if (s.length() <= n) {
      return s;
    } else {
      return s.substring(s.length() - n);
    }
  }

  public static int rotateBackwardExclusive(int x, int min, int max) {
    return rotateBackwardInclusive(x, min, max - 1);
  }

  public static int rotateBackwardInclusive(int x, int min, int max) {
    return (x <= min || x > max) ? max : x - 1;
  }

  public static int rotateForwardExclusive(int x, int min, int max) {
    return rotateForwardInclusive(x, min, max - 1);
  }

  public static int rotateForwardInclusive(int x, int min, int max) {
    return (x < min || x >= max) ? min : x + 1;
  }

  /**
   * Rounds {@code x} with a specified scale {@code dec}.
   * 
   * @param x a value to round
   * @param dec rounding scale
   * @return a rounded value.
   */
  public static double round(double x, int dec) {
    Assert.isScale(dec);
    if (Double.isInfinite(x) || Double.isNaN(x)) {
      return BeeConst.DOUBLE_ZERO;
    }

    if (dec == 0) {
      if (x > BeeConst.DOUBLE_ZERO) {
        return Math.floor(x) + Math.round(x - Math.floor(x));
      }
      if (x < BeeConst.DOUBLE_ZERO) {
        return Math.ceil(x) + Math.round(x - Math.ceil(x));
      }
      return BeeConst.DOUBLE_ZERO;
    }

    double z = Math.pow(10, dec);
    double y = x * z;
    if (isLong(y)) {
      return Math.round(y) / z;
    }

    if (x > BeeConst.DOUBLE_ZERO) {
      return Math.floor(x) + round(x - Math.floor(x), dec);
    }
    if (x < BeeConst.DOUBLE_ZERO) {
      return Math.ceil(x) + round(x - Math.ceil(x), dec);
    }
    return BeeConst.DOUBLE_ZERO;
  }

  public static boolean same(char c1, char c2) {
    if (c1 == c2) {
      return true;
    }
    return Character.toLowerCase(c1) == Character.toLowerCase(c2);
  }

  /**
   * Checks if {@code s1} and {@code s2} are same. Note: values are trimmed, case is ignored.
   * 
   * @param s1 value to check
   * @param s2 value to check
   * @return true if values are same, otherwise false.
   */
  public static boolean same(String s1, String s2) {
    if (isEmpty(s1)) {
      return isEmpty(s2);
    }
    if (isEmpty(s2)) {
      return isEmpty(s1);
    }
    return s1.trim().equalsIgnoreCase(s2.trim());
  }

  public static boolean sameSign(int i1, int i2) {
    return Integer.signum(i1) == Integer.signum(i2);
  }

  public static double scaleNormalizedToRange(double x, double min, double max) {
    double z;

    if (min == max) {
      z = min;
    } else if (x <= 0) {
      z = min;
    } else if (x >= 1) {
      z = max;
    } else if (min > max) {
      z = (1 - x) * (min - max) + max;
    } else {
      z = x * (max - min) + min;
    }
    return z;
  }

  /**
   * Creates a String with defined length of spaces.
   * 
   * @param l spaces count
   * @return a created String.
   */
  public static String space(int l) {
    if (l == 1) {
      return BeeConst.STRING_SPACE;
    } else if (l < 1) {
      return BeeConst.STRING_EMPTY;
    } else {
      return replicate(BeeConst.CHAR_SPACE, l);
    }
  }

  /**
   * Splits {@code str} with a specified separator {@code separator}.
   * 
   * @param str a string to split
   * @param separators a separator used for splitting
   * @return a String array splitted using the separator.
   */
  public static String[] split(String str, Object separators) {
    if (str == null) {
      return null;
    }
    int len = str.length();
    if (len == 0) {
      return BeeConst.EMPTY_STRING_ARRAY;
    }

    String sep = normSep(separators, BeeConst.STRING_SPACE);
    int z = sep.length();
    char ch = sep.charAt(0);

    if (z == 1 && str.indexOf(ch) < 0) {
      return new String[] {str.trim()};
    }

    List<String> lst = new ArrayList<String>();
    int i = 0;
    int start = 0;

    boolean match = false;
    boolean ok;

    while (i < len) {
      if (z == 1) {
        ok = (str.charAt(i) == ch);
      } else {
        ok = (sep.indexOf(str.charAt(i)) >= 0);
      }

      if (ok) {
        if (match) {
          lst.add(str.substring(start, i).trim());
          match = false;
        }
        start = ++i;
      } else {
        match = true;
        i++;
      }
    }

    if (match) {
      lst.add(str.substring(start, i).trim());
    }
    return lst.toArray(new String[lst.size()]);
  }

  /**
   * Checks if {@code s1} and {@code s2} starts the same. The shorter String is compared with the
   * start of the longer String.
   * 
   * @param s1 the first String for comparing
   * @param s2 the second String for comparing
   * @return true if {@code s1} and {@code s2} starts the same, otherwise false.
   */
  public static boolean startsSame(String s1, String s2) {
    if (isEmpty(s1) || isEmpty(s2)) {
      return false;
    }

    int len = Math.min(s1.trim().length(), s2.trim().length());
    if (len > 0) {
      return s1.trim().substring(0, len).equalsIgnoreCase(s2.trim().substring(0, len));
    } else {
      return false;
    }
  }

  public static boolean startsWith(String s, char c) {
    if (s == null || s.trim().length() <= 0) {
      return false;
    }
    return same(s.trim().charAt(0), c);
  }

  public static boolean startsWith(String str, String pfx) {
    if (isEmpty(str) || isEmpty(pfx)) {
      return false;
    }

    int len = pfx.trim().length();
    if (len > 0 && len <= str.trim().length()) {
      return str.trim().substring(0, len).equalsIgnoreCase(pfx.trim());
    } else {
      return false;
    }
  }

  /**
   * Checks if {@code x} is a Boolean value.
   * 
   * @param x value to check
   * @return true if the value is Boolean, otherwise false.
   */
  public static boolean toBoolean(int x) {
    return x == BeeConst.INT_TRUE;
  }

  /**
   * Checks if {@code s} is a Boolean value.
   * 
   * @param s value to check
   * @return true if the value is Boolean, otherwise false.
   */
  public static boolean toBoolean(String s) {
    if (isEmpty(s)) {
      return false;
    }
    return BeeConst.isTrue(s);
  }

  public static Boolean toBooleanOrNull(String s) {
    if (isEmpty(s)) {
      return null;
    }
    return toBoolean(s);
  }

  public static char toChar(int x) {
    if (x < Character.MIN_VALUE) {
      return Character.MIN_VALUE;
    }
    if (x >= Character.MAX_VALUE) {
      return Character.MAX_VALUE;
    }
    return (char) x;
  }

  public static BigDecimal toDecimalOrNull(Double x) {
    if (x == null) {
      return null;
    } else {
      return BigDecimal.valueOf(x);
    }
  }

  public static BigDecimal toDecimalOrNull(Integer x) {
    if (x == null) {
      return null;
    } else {
      return BigDecimal.valueOf(x);
    }
  }

  public static BigDecimal toDecimalOrNull(Long x) {
    if (x == null) {
      return null;
    } else {
      return BigDecimal.valueOf(x);
    }
  }

  public static BigDecimal toDecimalOrNull(String s) {
    if (isEmpty(s)) {
      return null;
    }
    BigDecimal d;

    try {
      d = new BigDecimal(s.trim());
    } catch (NumberFormatException ex) {
      d = null;
    }
    return d;
  }

  /**
   * Converts a String value {@code s} to Double.
   * 
   * @param s a string to convert
   * @return a corresponding double value
   * @throws NumberFormatException ex
   */
  public static double toDouble(String s) {
    if (isEmpty(s)) {
      return BeeConst.DOUBLE_ZERO;
    }
    double d;

    try {
      d = Double.parseDouble(s.trim());
    } catch (NumberFormatException ex) {
      d = BeeConst.DOUBLE_ZERO;
    }
    return d;
  }

  public static Double toDoubleOrNull(String s) {
    if (isEmpty(s)) {
      return null;
    }
    return toDouble(s);
  }

  /**
   * Converts a String value {@code s} to Float.
   * 
   * @param s a string to convert
   * @return a corresponding Float value
   * @throws NumberFormatException ex
   */
  public static float toFloat(String s) {
    if (isEmpty(s)) {
      return BeeConst.FLOAT_ZERO;
    }
    float i;

    try {
      i = Float.parseFloat(s.trim());
    } catch (NumberFormatException ex) {
      i = BeeConst.FLOAT_ZERO;
    }
    return i;
  }

  /**
   * Converts {@code b} to Integer.
   * 
   * @param b value to convert
   * @return 1 if b is true, otherwise 0.
   */
  public static int toInt(boolean b) {
    return b ? BeeConst.INT_TRUE : BeeConst.INT_FALSE;
  }

  public static int toInt(Double d) {
    if (!isDouble(d)) {
      return 0;
    }
    if (d <= Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    if (d >= Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    return d.intValue();
  }

  /**
   * Converts a String value {@code s} to Integer.
   * 
   * @param s value to convert
   * @return a corresponding Integer value
   * @throws NumberFormatException ex
   */
  public static int toInt(String s) {
    if (isEmpty(s)) {
      return 0;
    }
    int i;

    try {
      i = Integer.parseInt(s.trim());
    } catch (NumberFormatException ex) {
      if (isDouble(s)) {
        i = toInt(toDouble(s));
      } else {
        i = 0;
      }
    }
    return i;
  }

  public static Integer toIntOrNull(String s) {
    if (isEmpty(s)) {
      return null;
    }
    return toInt(s);
  }

  /**
   * Adds leading zeros to {@code x}. Converts {@code x} to a String and if it's length is less than
   * {@code n} adds them.
   * 
   * @param x a value to add leading zeros to
   * @param n length to add zeros
   * @return a String representation of {@code x} with leading zeros.
   */
  public static String toLeadingZeroes(int x, int n) {
    if (x >= 0 && n > 0) {
      return padLeft(((Integer) x).toString(), n, BeeConst.CHAR_ZERO);
    } else {
      return ((Integer) x).toString();
    }
  }

  public static long toLong(Double d) {
    if (!isDouble(d)) {
      return 0L;
    }
    if (d <= Long.MIN_VALUE) {
      return Long.MIN_VALUE;
    }
    if (d >= Long.MAX_VALUE) {
      return Long.MAX_VALUE;
    }
    return d.longValue();
  }

  /**
   * Converts a String value {@code s} to Long.
   * 
   * @param s value to convert
   * @return a corresponding Long value
   * @throws NumberFormatException ex
   */
  public static long toLong(String s) {
    if (isEmpty(s)) {
      return 0L;
    }
    long x;

    try {
      x = Long.parseLong(s.trim());
    } catch (NumberFormatException ex) {
      if (isDouble(s)) {
        x = toLong(toDouble(s));
      } else {
        x = 0L;
      }
    }
    return x;
  }

  public static Long toLongOrNull(String s) {
    if (isEmpty(s)) {
      return null;
    }
    return toLong(s);
  }

  public static int toNonNegativeInt(Double d) {
    return toNonNegativeInt(toInt(d));
  }

  public static int toNonNegativeInt(Integer x) {
    if (x == null) {
      return 0;
    }
    return Math.max(x, 0);
  }

  /**
   * Converts milliseconds {@code millis} to seconds. E.g 6010 is converted to 6.010.
   * 
   * @param millis value to convert
   * @return seconds.
   */
  public static String toSeconds(long millis) {
    return Long.toString(millis / 1000) + BeeConst.STRING_POINT
        + toLeadingZeroes((int) (millis % 1000), 3);
  }

  public static String toString(BigDecimal bd) {
    if (bd == null) {
      return null;
    } else {
      return bd.toString();
    }
  }

  /**
   * Converts a Boolean value {@code b} to a String value.
   * 
   * @param b value to convert
   * @return a String representation of {@code b}
   */
  public static String toString(boolean b) {
    return b ? BeeConst.STRING_TRUE : BeeConst.STRING_FALSE;
  }

  public static String toString(char c) {
    return String.valueOf(c);
  }

  /**
   * Converts a Double value {@code x} to a String value. Removes trailing zeroes.
   * 
   * @param x value to convert
   * @return a String representation of {@code x}
   */
  public static String toString(double x) {
    return removeTrailingZeros(Double.toString(x));
  }

  /**
   * Converts an Integer value {@code x} to a String value.
   * 
   * @param x value to convert
   * @return String representation of {@code x}
   */
  public static String toString(int x) {
    return Integer.toString(x);
  }

  /**
   * Converts an Long value {@code x} to a String value.
   * 
   * @param x value to convert
   * @return String representation of {@code x}
   */
  public static String toString(long x) {
    return Long.toString(x);
  }

  /**
   * Transforms an Object {@code x} to a String representation. In general, this method returns a
   * string that "textually represents" this object. String type Objects are trimmed.
   * 
   * @param x value to transform.
   * @return a string that "textually represents" this object.
   */
  public static String transform(Object x) {
    String s;

    if (x == null) {
      s = BeeConst.STRING_EMPTY;
    } else if (x instanceof String) {
      s = ((String) x).trim();
    } else if (x instanceof Transformable) {
      s = ((Transformable) x).transform();
    } else {
      s = x.toString();
    }
    return s;
  }

  /**
   * Transforms a Collection {@code col} to a String representation using the specified separators
   * {@code sep}. Each level of recursion use the next separator.
   * 
   * @param col a Collection to transform.
   * @param sep separators used to transform. Uses a default separator if none are specified.
   * @return a String representation of the Collection {@code col}.
   */
  public static String transformCollection(Collection<?> col, Object... sep) {
    if (isEmpty(col)) {
      return BeeConst.STRING_EMPTY;
    }

    int cSep = (sep == null) ? 0 : sep.length;
    String z = cSep > 0 ? normSep(sep[0]) : BeeConst.DEFAULT_LIST_SEPARATOR;

    StringBuilder sb = new StringBuilder();
    Object[] nextSep;

    if (cSep > 1) {
      nextSep = new Object[cSep - 1];
      for (int i = 0; i < cSep - 1; i++) {
        nextSep[i] = sep[i + 1];
      }
    } else {
      nextSep = new String[] {z};
    }

    for (Object el : col) {
      if (sb.length() > 0) {
        sb.append(z);
      }
      sb.append(transformGeneric(el, nextSep));
    }
    return sb.toString();
  }

  /**
   * Transforms an Enumeration {@code src} to a String representation using the specified separators
   * {@code sep}. Each level of recursion use the next separator.
   * 
   * @param src an Enumeration to transform.
   * @param sep separators used to transform. Uses a default separator if none are specified.
   * @return String representation of the Enumeration {@code src}.
   */
  public static String transformEnumeration(Enumeration<?> src, Object... sep) {
    if (isEmpty(src)) {
      return BeeConst.STRING_EMPTY;
    }
    int cSep = (sep == null) ? 0 : sep.length;
    String z = cSep > 0 ? normSep(sep[0]) : BeeConst.DEFAULT_LIST_SEPARATOR;

    StringBuilder sb = new StringBuilder();
    Object el;
    Object[] nextSep;

    if (cSep > 1) {
      nextSep = new Object[cSep - 1];
      for (int i = 0; i < cSep - 1; i++) {
        nextSep[i] = sep[i + 1];
      }
    } else {
      nextSep = new String[] {z};
    }

    while (src.hasMoreElements()) {
      el = src.nextElement();
      if (sb.length() > 0) {
        sb.append(z);
      }
      sb.append(transformGeneric(el, nextSep));
    }
    return sb.toString();
  }

  /**
   * Transforms an Object {@code x} to a String representation using the specified separator
   * {@code sep}. Each level of recursion use the next separator.
   * 
   * @param x an Object to transform.
   * @param sep separators for transforming Collections,Maps,Arrays and Enumerations. Uses a default
   *          separator if none are specified.
   * @return a String representation of the Object {@code x}.
   */
  public static String transformGeneric(Object x, Object... sep) {
    if (x instanceof Collection) {
      return transformCollection((Collection<?>) x, sep);
    } else if (x instanceof Map) {
      return transformMap((Map<?, ?>) x, sep);
    } else if (ArrayUtils.isArray(x)) {
      return ArrayUtils.transform(x, sep);
    } else if (x instanceof Enumeration) {
      return transformEnumeration((Enumeration<?>) x, sep);
    } else {
      return transform(x);
    }
  }

  /**
   * Transforms a Map {@code map} to a String representation using the specified separators
   * {@code sep}. Each level of recursion use the next separator.
   * 
   * @param map a Map to transform.
   * @param sep separators used to transform. Uses a default separator if none are specified.
   * @return String representation of the Map {@code map}.
   */
  public static String transformMap(Map<?, ?> map, Object... sep) {
    if (isEmpty(map)) {
      return BeeConst.STRING_EMPTY;
    }
    int cSep = (sep == null) ? 0 : sep.length;
    String z = cSep > 0 ? normSep(sep[0]) : BeeConst.DEFAULT_LIST_SEPARATOR;

    StringBuilder sb = new StringBuilder();

    Object key, value;
    Object[] nextSep;

    if (cSep > 1) {
      nextSep = new Object[cSep - 1];
      for (int i = 0; i < cSep - 1; i++) {
        nextSep[i] = sep[i + 1];
      }
    } else {
      nextSep = new String[] {z};
    }

    for (Map.Entry<?, ?> el : map.entrySet()) {
      key = el.getKey();
      value = el.getValue();

      if (sb.length() > 0) {
        sb.append(z);
      }
      sb.append(NameUtils.addName(transform(key), transformGeneric(value, nextSep)));
    }
    return sb.toString();
  }

  /**
   * Transforms an Object {@code x} to a String representation. In general, this method returns a
   * string that "textually represents" this object. String type Objects are <b>not trimmed</b>.
   * 
   * @param x a value to transform
   * @return
   */
  public static String transformNoTrim(Object x) {
    if (x instanceof String) {
      return (String) x;
    } else {
      return transform(x);
    }
  }

  /**
   * Transforms Objects {@code opt} to a String representation. {@code opt} are coupled of two. A
   * default separator is used.
   * <p>
   * E.g {@code transformOption("name", "John", "name2", "Dan") results in
   * "name=John;name2=Dan"}.
   * </p>
   * 
   * @param opt Objects to transform
   * @return a String representation of Objects {@code opt}.
   */
  public static String transformOptions(Object... opt) {
    Assert.notNull(opt);
    int c = opt.length;
    Assert.parameterCount(c, 2);

    StringBuilder sb = new StringBuilder();
    String el;

    for (int i = 0; i < c - 1; i += 2) {
      if (opt[i] instanceof String) {
        el = NameUtils.addName((String) opt[i], opt[i + 1]);
        if (!isEmpty(el)) {
          if (sb.length() > 0) {
            sb.append(BeeConst.DEFAULT_OPTION_SEPARATOR);
          }
          sb.append(el);
        }
      }
    }
    return sb.toString();
  }

  public static String trim(String s) {
    return (s == null) ? BeeConst.STRING_EMPTY : s.trim();
  }

  public static String trimLeft(String s) {
    if (s == null || s.isEmpty()) {
      return BeeConst.STRING_EMPTY;
    }

    int len = s.length();
    int pos = 0;

    while (pos < len && isWhitespace(s.charAt(pos))) {
      pos++;
    }

    if (pos >= len) {
      return BeeConst.STRING_EMPTY;
    } else if (pos > 0) {
      return s.substring(pos);
    } else {
      return s;
    }
  }

  public static String trimRight(String s) {
    if (s == null || s.isEmpty()) {
      return BeeConst.STRING_EMPTY;
    }

    int len = s.length();
    int pos = len;

    while (pos > 0 && isWhitespace(s.charAt(pos - 1))) {
      pos--;
    }

    if (pos <= 0) {
      return BeeConst.STRING_EMPTY;
    } else if (pos < len) {
      return s.substring(0, pos);
    } else {
      return s;
    }
  }

  /**
   * Null-safe Boolean unboxing.
   * 
   * @param box an Boolean to unbox
   * @return unboxed boolean value or false if {@code box} is null
   */
  public static boolean unbox(Boolean box) {
    return (box == null) ? false : box;
  }

  /**
   * Null-safe Integer unboxing.
   * 
   * @param box an Integer to unbox
   * @return unboxed int value or 0 if {@code box} is null
   */
  public static int unbox(Integer box) {
    return (box == null) ? 0 : box;
  }

  /**
   * Null-safe Long unboxing.
   * 
   * @param box an Long to unbox
   * @return unboxed long value or 0 if {@code box} is null
   */
  public static long unbox(Long box) {
    return (box == null) ? 0 : box;
  }

  /**
   * Null-safe collection union.
   */
  public static <T> Set<T> union(Collection<? extends T> col1, Collection<? extends T> col2) {
    Set<T> result = new HashSet<T>();

    if (col1 != null) {
      result.addAll(col1);
    }
    if (col2 != null) {
      result.addAll(col2);
    }
    return result;
  }

  public static <T> Set<T> union(Collection<? extends T> col1, Collection<? extends T> col2,
      Collection<? extends T> col3) {
    Set<T> result = new HashSet<T>();

    if (col1 != null) {
      result.addAll(col1);
    }
    if (col2 != null) {
      result.addAll(col2);
    }
    if (col3 != null) {
      result.addAll(col3);
    }
    return result;
  }
  
  /**
   * Searches for an Integer value from a String {@code s}.
   * 
   * @param s the String to search value from
   * @return an Integer value if found, otherwise 0;
   */
  public static int val(String s) {
    if (s == null) {
      return 0;
    }
    int len = s.length();
    if (len <= 0) {
      return 0;
    }

    int start = 0;
    while (start < len && s.charAt(start) <= BeeConst.CHAR_SPACE) {
      start++;
    }
    if (start >= len) {
      return 0;
    }

    int end = start;
    if (s.charAt(start) == BeeConst.CHAR_MINUS) {
      if (len <= start + 1 || !isDigit(s.charAt(start + 1))) {
        return 0;
      }
      end++;
    }

    while (end < len && isDigit(s.charAt(end))) {
      end++;
    }
    if (end <= start) {
      return 0;
    }

    return toInt(s.substring(start, end));
  }

  /**
   * Returns a zero value of a specified Object {@code x}.
   * 
   * @param x a value to return zero
   * @return a zero value of a specified Object {@code x}.
   */
  @SuppressWarnings("unchecked")
  public static <T> T zero(T x) {
    if (x == null) {
      return null;
    } else if (x instanceof Integer) {
      return (T) Integer.valueOf(0);
    } else if (x instanceof Byte) {
      return (T) Byte.valueOf((byte) 0);
    } else if (x instanceof Short) {
      return (T) Short.valueOf((short) 0);
    } else if (x instanceof Long) {
      return (T) Long.valueOf(0);
    } else if (x instanceof BigInteger) {
      return (T) BigInteger.ZERO;
    } else if (x instanceof BigDecimal) {
      return (T) BigDecimal.ZERO;
    } else if (x instanceof Float) {
      return (T) Float.valueOf(BeeConst.FLOAT_ZERO);
    } else if (x instanceof Double) {
      return (T) Double.valueOf(BeeConst.DOUBLE_ZERO);
    } else {
      return (T) Integer.valueOf(0);
    }
  }

  private BeeUtils() {
  }
}
