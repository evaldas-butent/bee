package com.butent.bee.egg.shared.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.BeeType;
import com.butent.bee.egg.shared.Transformable;

public abstract class BeeUtils {
  private static int NAME_COUNTER = 0;

  public static boolean isEmpty(Object x) {
    boolean ok;

    if (x == null)
      ok = true;
    else if (x instanceof String)
      ok = ((String) x).isEmpty() || ((String) x).trim().isEmpty();
    else if (x instanceof CharSequence)
      ok = ((CharSequence) x).length() == 0
          || ((CharSequence) x).toString().trim().isEmpty();
    else if (x instanceof Number)
      ok = isZero(x);
    else if (x instanceof Boolean)
      ok = !(Boolean) x;
    else if (x instanceof Transformable)
      ok = ((Transformable) x).transform().isEmpty();
    else if (x instanceof Collection)
      ok = ((Collection<?>) x).isEmpty();
    else if (x instanceof Map)
      ok = ((Map<?, ?>) x).isEmpty();
    else if (isArray(x))
      ok = ((Object[]) x).length == 0;
    else if (x instanceof Enumeration)
      ok = !((Enumeration<?>) x).hasMoreElements();
    else
      ok = false;

    return ok;
  }

  public static boolean isEmpty(Object x, int... orType) {
    if (filterType(x, orType))
      return false;
    else
      return isEmpty(x);
  }

  public static boolean isZero(byte x) {
    return x == 0;
  }

  public static boolean isZero(int x) {
    return x == 0;
  }

  public static boolean isZero(long x) {
    return x == 0L;
  }

  public static boolean isZero(float x) {
    return x == Float.valueOf(0.0f);
  }

  public static boolean isZero(double x) {
    return x == Double.valueOf(0.0d);
  }

  public static boolean isZero(BigDecimal x) {
    return x == BigDecimal.ZERO;
  }

  public static boolean isZero(BigInteger x) {
    return x == BigInteger.ZERO;
  }

  public static boolean isZero(Object x) {
    if (x instanceof Number)
      return ((Number) x).doubleValue() == Double.valueOf(0.0d);
    else
      return false;
  }

  public static String transformCollection(Collection<?> lst, Object... sep) {
    if (isEmpty(lst))
      return BeeConst.STRING_EMPTY;
    int cSep = sep.length;
    String z = cSep > 0 ? normSep(sep[0]) : BeeConst.DEFAULT_LIST_SEPARATOR;

    StringBuilder sb = new StringBuilder();
    Object el;
    Object[] nextSep;

    if (cSep > 1) {
      nextSep = new Object[cSep - 1];
      for (int i = 0; i < cSep - 1; i++)
        nextSep[i] = sep[i + 1];
    } else
      nextSep = new String[] { z };

    for (Iterator<?> it = lst.iterator(); it.hasNext();) {
      el = it.next();
      if (sb.length() > 0)
        sb.append(z);
      sb.append(transform(el, nextSep));
    }

    return sb.toString();
  }

  public static String transformMap(Map<?, ?> lst, Object... sep) {
    if (isEmpty(lst))
      return BeeConst.STRING_EMPTY;
    int cSep = sep.length;
    String z = cSep > 0 ? normSep(sep[0]) : BeeConst.DEFAULT_LIST_SEPARATOR;

    StringBuilder sb = new StringBuilder();

    Object key, value;
    Object[] nextSep;

    if (cSep > 1) {
      nextSep = new Object[cSep - 1];
      for (int i = 0; i < cSep - 1; i++)
        nextSep[i] = sep[i + 1];
    } else
      nextSep = new String[] { z };

    for (Map.Entry<?, ?> el : lst.entrySet()) {
      key = el.getKey();
      value = el.getValue();

      if (sb.length() > 0)
        sb.append(z);
      sb.append(addName(transform(key), transform(value, nextSep)));
    }

    return sb.toString();
  }

  public static String transformArray(Object arr, Object... sep) {
    if (isEmpty(arr))
      return BeeConst.STRING_EMPTY;
    int cSep = sep.length;
    String z = cSep > 0 ? normSep(sep[0]) : BeeConst.DEFAULT_LIST_SEPARATOR;

    StringBuilder sb = new StringBuilder();
    Object el;
    Object[] nextSep;

    if (cSep > 1) {
      nextSep = new Object[cSep - 1];
      for (int i = 0; i < cSep - 1; i++)
        nextSep[i] = sep[i + 1];
    } else
      nextSep = new String[] { z };

    int r = ((Object[]) arr).length;

    for (int i = 0; i < r; i++) {
      el = ((Object[]) arr)[i];
      if (i > 0)
        sb.append(z);
      sb.append(transform(el, nextSep));
    }

    return sb.toString();
  }

  public static String transformEnumeration(Enumeration<?> lst, Object... sep) {
    if (isEmpty(lst))
      return BeeConst.STRING_EMPTY;
    int cSep = sep.length;
    String z = cSep > 0 ? normSep(sep[0]) : BeeConst.DEFAULT_LIST_SEPARATOR;

    StringBuilder sb = new StringBuilder();
    Object el;
    Object[] nextSep;

    if (cSep > 1) {
      nextSep = new Object[cSep - 1];
      for (int i = 0; i < cSep - 1; i++)
        nextSep[i] = sep[i + 1];
    } else
      nextSep = new String[] { z };

    while (lst.hasMoreElements()) {
      el = lst.nextElement();
      if (sb.length() > 0)
        sb.append(z);
      sb.append(transform(el, nextSep));
    }

    return sb.toString();
  }

  public static String transform(Object x, Object... sep) {
    if (x instanceof Collection)
      return transformCollection((Collection<?>) x, sep);
    else if (x instanceof Map)
      return transformMap((Map<?, ?>) x, sep);
    else if (isArray(x))
      return transformArray(x, sep);
    else if (x instanceof Enumeration)
      return transformEnumeration((Enumeration<?>) x, sep);
    else
      return transform(x);
  }

  public static String transform(Object x) {
    String s;

    if (x == null)
      s = BeeConst.STRING_EMPTY;
    else if (x instanceof String)
      s = ((String) x).trim();
    else if (x instanceof Transformable)
      s = ((Transformable) x).transform();
    else if (x instanceof Exception)
      s = ((Exception) x).getMessage();
    else
      s = x.toString();

    return s;
  }

  public static String transformOptions(Object... opt) {
    int c = opt.length;
    if (c < 2)
      return null;

    StringBuilder sb = new StringBuilder();
    String el;

    for (int i = 0; i < c - 1; i += 2)
      if (opt[i] instanceof String) {
        el = addName((String) opt[i], opt[i + 1]);
        if (!isEmpty(el)) {
          if (sb.length() > 0)
            sb.append(BeeConst.DEFAULT_OPTION_SEPARATOR);
          sb.append(el);
        }
      }

    return sb.toString();
  }

  public static String addName(String nm, Object v) {
    if (isEmpty(v, BeeType.TYPE_NUMBER + BeeType.TYPE_BOOLEAN))
      return BeeConst.STRING_EMPTY;
    else if (isEmpty(nm))
      return transform(v);
    else
      return nm.trim() + BeeConst.DEFAULT_VALUE_SEPARATOR + transform(v);
  }

  public static String ifString(Object x, String def) {
    if (x instanceof String && !isEmpty(x))
      return (String) x;
    else
      return def;
  }

  public static String space(int l) {
    if (l == 1)
      return BeeConst.STRING_SPACE;
    else if (l < 1)
      return BeeConst.STRING_EMPTY;
    else
      return replicate(BeeConst.CHAR_SPACE, l);
  }

  public static String normSep(Object x) {
    String sep;

    if (x instanceof String)
      sep = (String) x;
    else if (x instanceof Number)
      sep = space(((Number) x).intValue());
    else if (x instanceof Character)
      sep = new String(new char[] { (Character) x });
    else
      sep = BeeConst.DEFAULT_LIST_SEPARATOR;

    return sep;
  }

  public static String concat(Object... x) {
    int c = x.length;
    if (c <= 1)
      return BeeConst.STRING_EMPTY;

    StringBuilder s = new StringBuilder();
    String sep = normSep(x[0]);

    for (int i = 1; i < c; i++) {
      if (!isEmpty(x[i], BeeType.TYPE_NUMBER)) {
        if (s.length() > 0)
          s.append(sep);
        s.append(transform(x[i], sep));
      }
    }

    return s.toString();
  }

  public static String left(String s, int n) {
    if (s == null)
      return null;
    else if (n <= 0)
      return BeeConst.STRING_EMPTY;
    else if (s.length() <= n)
      return s;
    else
      return s.substring(0, n);
  }

  public static String replicate(char z, int n) {
    if (n > 0) {
      char[] arr = new char[n];
      Arrays.fill(arr, z);
      return new String(arr);
    } else
      return BeeConst.STRING_EMPTY;
  }

  public static String padLeft(String s, int n, char z) {
    if (s == null)
      return null;
    else if (n <= 0)
      return BeeConst.STRING_EMPTY;
    else if (s.length() >= n)
      return s;
    else
      return replicate(z, n - s.length()) + s;
  }

  public static String toLeadingZeroes(int x, int n) {
    if (x >= 0 && n > 0)
      return padLeft(((Integer) x).toString(), n, BeeConst.CHAR_ZERO);
    else
      return ((Integer) x).toString();
  }

  public static int compare(String s1, String s2) {
    if (isEmpty(s1))
      if (isEmpty(s2))
        return BeeConst.COMPARE_EQUAL;
      else
        return BeeConst.COMPARE_LESS;
    else if (isEmpty(s2))
      return BeeConst.COMPARE_MORE;
    else
      return s1.compareTo(s2);
  }

  public static int compare(int x1, int x2) {
    if (x1 == x2)
      return BeeConst.COMPARE_EQUAL;
    else if (x1 < x2)
      return BeeConst.COMPARE_LESS;
    else
      return BeeConst.COMPARE_MORE;
  }

  public static int compare(double x1, double x2) {
    if (x1 == x2 || ((Double) x1).equals(x2))
      return BeeConst.COMPARE_EQUAL;
    else if (x1 < x2)
      return BeeConst.COMPARE_LESS;
    else
      return BeeConst.COMPARE_MORE;
  }

  @SuppressWarnings("unchecked")
  public static <T> int compare(Comparable<T> x1, Comparable<T> x2) {
    if (isEmpty(x1))
      if (isEmpty(x2))
        return BeeConst.COMPARE_EQUAL;
      else
        return x1.compareTo((T) x2);
    else if (isEmpty(x2))
      return BeeConst.COMPARE_MORE;
    else
      return x1.compareTo((T) x2);
  }

  public static int compare(Object x1, Object x2) {
    if (isEmpty(x1))
      if (isEmpty(x2))
        return BeeConst.COMPARE_EQUAL;
      else
        return BeeConst.COMPARE_LESS;
    else if (isEmpty(x2))
      return BeeConst.COMPARE_MORE;
    else if (x1 == x2 || x1.equals(x2))
      return BeeConst.COMPARE_EQUAL;
    else
      return x1.toString().compareTo(x2.toString());
  }

  public static boolean isTrue(Object obj) {
    if (obj == null)
      return false;
    else if (obj instanceof Boolean)
      return (Boolean) obj;
    else
      return false;
  }

  public static boolean inList(String x, String... lst) {
    Assert.notEmpty(x);
    Assert.parameterCount(lst.length + 1, 2);
    boolean ok = false;

    for (int i = 0; i < lst.length; i++)
      if (x.equals(lst[i])) {
        ok = true;
        break;
      }

    return ok;
  }

  public static boolean inListIgnoreCase(String x, String... lst) {
    Assert.notEmpty(x);
    Assert.parameterCount(lst.length + 1, 2);
    boolean ok = false;

    for (int i = 0; i < lst.length; i++)
      if (x.equalsIgnoreCase(lst[i])) {
        ok = true;
        break;
      }

    return ok;
  }

  public static boolean inListSame(String x, String... lst) {
    Assert.notEmpty(x);
    Assert.parameterCount(lst.length + 1, 2);
    boolean ok = false;

    String z = x.trim().toLowerCase();

    for (int i = 0; i < lst.length; i++) {
      if (lst[i] == null)
        continue;
      if (z.equalsIgnoreCase(lst[i].trim())) {
        ok = true;
        break;
      }
    }

    return ok;
  }

  public static boolean inList(int x, int... lst) {
    Assert.parameterCount(lst.length + 1, 2);
    boolean ok = false;

    for (int i = 0; i < lst.length; i++)
      if (x == lst[i]) {
        ok = true;
        break;
      }

    return ok;
  }

  public static String createUniqueName(String pfx) {
    NAME_COUNTER++;

    if (pfx.isEmpty())
      return transform(NAME_COUNTER);
    else
      return pfx.trim() + NAME_COUNTER;
  }

  public static String createUniqueName() {
    return createUniqueName(null);
  }

  public static boolean isArray(Object obj) {
    return obj instanceof Object[];
  }

  public static boolean instanceOfIntegerType(Object x) {
    if (x == null)
      return false;
    else
      return (x instanceof Byte || x instanceof Short || x instanceof Integer
          || x instanceof Long || x instanceof BigInteger || x instanceof BigDecimal);
  }

  public static boolean instanceOfFloatingPoint(Object x) {
    if (x == null)
      return false;
    else
      return (x instanceof Float || x instanceof Double);
  }

  public static boolean instanceOfStringType(Object x) {
    if (x == null)
      return false;
    else
      return (x instanceof String || x instanceof StringBuilder || x instanceof StringBuffer);
  }

  public static boolean instanceOfDateTime(Object x) {
    if (x == null)
      return false;
    else
      return x instanceof BeeDate;
  }

  public static boolean filterType(Object x, int... types) {
    boolean ok = false;
    if (x == null || types.length <= 0)
      return ok;

    int tp;

    if (x instanceof Boolean)
      tp = BeeType.TYPE_BOOLEAN;
    else if (instanceOfStringType(x))
      tp = BeeType.TYPE_STRING;
    else if (x instanceof Character)
      tp = BeeType.TYPE_CHAR;
    else if (x instanceof Number) {
      tp = BeeType.TYPE_NUMBER;
      if (instanceOfIntegerType(x))
        tp += BeeType.TYPE_INT;
      if (instanceOfFloatingPoint(x))
        tp += BeeType.TYPE_DOUBLE;
    } else if (instanceOfDateTime(x))
      tp = BeeType.TYPE_DATE;
    else
      tp = BeeType.TYPE_UNKNOWN;

    for (int i = 0; i < types.length; i++)
      if ((tp & types[i]) != 0) {
        ok = true;
        break;
      }

    return ok;
  }

  public static <T> T getElement(T[] arr, int idx) {
    if (arr == null)
      return null;
    else if (idx >= 0 && idx < arr.length)
      return arr[idx];
    else
      return null;
  }

  public static boolean context(CharSequence ctxt, CharSequence src) {
    if (ctxt == null || src == null || ctxt.length() == 0 || src.length() == 0)
      return false;
    else
      return src.toString().toLowerCase()
          .contains(ctxt.toString().toLowerCase());
  }

  public static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A'
        && c <= 'F');
  }

  public static boolean isHexString(String s) {
    if (isEmpty(s))
      return false;

    boolean ok = true;

    for (int i = 0; i < s.length(); i++)
      if (!isHexDigit(s.charAt(i))) {
        ok = false;
        break;
      }

    return ok;
  }

  public static String toHex(char c) {
    return padLeft(Integer.toHexString((int) c), 4, BeeConst.CHAR_ZERO);
  }

  public static String toHex(char[] arr) {
    if (isEmpty(arr))
      return null;
    else if (arr.length == 1)
      return toHex(arr[0]);
    else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < arr.length; i++)
        sb.append(toHex(arr[i]));
      return sb.toString();
    }
  }

  public static char[] fromHex(String s) {
    if (!isHexString(s))
      return null;

    if (s.length() <= 4) {
      return new char[] { (char) Integer.parseInt(s, 16) };
    } else {
      int z = s.length() % 4;
      int n;

      if (z == 0)
        n = s.length() / 4;
      else
        n = (s.length() - z) / 4 + 1;

      char[] arr = new char[n];
      String u;

      for (int i = 0; i < n; i++) {
        if (z == 0)
          u = s.substring(i * 4, (i + 1) * 4);
        else if (i == 0)
          u = s.substring(0, z);
        else
          u = s.substring(z + (i - 1) * 4, z + i * 4);

        arr[i] = (char) Integer.parseInt(u, 16);
      }

      return arr;
    }
  }

  public static boolean contains(CharSequence src, char[] sep) {
    boolean ok = false;
    if (src == null || src.length() == 0 || sep == null || sep.length == 0)
      return ok;

    int lenSrc = src.length();
    int lenSep = sep.length;

    if (lenSep == 1) {
      for (int i = 0; i < lenSrc; i++)
        if (src.charAt(i) == sep[0]) {
          ok = true;
          break;
        }
    } else if (lenSrc >= lenSep) {
      for (int i = 0; i <= lenSrc - lenSep; i++) {
        for (int j = 0; j < lenSep; j++) {
          if (src.charAt(i + j) == sep[j]) {
            ok = true;
          } else {
            ok = false;
            break;
          }
        }
        if (ok)
          break;
      }
    }

    return ok;
  }

  public static String bracket(Object x) {
    if (x == null)
      return BeeConst.STRING_EMPTY;
    else
      return BeeConst.STRING_OPEN_BRACKET + transform(x)
          + BeeConst.STRING_CLOSE_BRACKET;
  }

  public static int toInt(String s) {
    if (isEmpty(s))
      return 0;
    int i;

    try {
      i = Integer.parseInt(s);
    } catch (NumberFormatException ex) {
      i = 0;
    }
    return i;
  }

  public static String increment(String s) {
    return Integer.toString(toInt(s) + 1);
  }

  public static String increment(Object obj) {
    return increment(transform(obj));
  }

  public static boolean between(int x, int min, int max) {
    return x >= min && x <= max;
  }

  @SuppressWarnings("unchecked")
  public static <T> T iif(Object... obj) {
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

  public static String progress(int pos, int tot) {
    return Integer.toString(pos) + BeeConst.DEFAULT_PROGRESS_SEPARATOR
        + Integer.toString(tot);
  }

  public static boolean equalsTrim(String s1, String s2) {
    if (s1 == null)
      return s2 == null;
    if (s2 == null)
      return isEmpty(s1);
    else
      return s1.trim().equals(s2.trim());
  }

  public static boolean same(String s1, String s2) {
    if (s1 == null)
      return s2 == null;
    if (s2 == null)
      return isEmpty(s1);
    else
      return s1.trim().equalsIgnoreCase(s2.trim());
  }

  public static String toString(boolean b) {
    return b ? BeeConst.STRING_TRUE : BeeConst.STRING_FALSE;
  }

  public static int toInt(boolean b) {
    return b ? BeeConst.INT_TRUE : BeeConst.INT_FALSE;
  }

  public static boolean toBoolean(String s) {
    if (s == null)
      return false;
    else
      return same(s, BeeConst.STRING_TRUE) || same(s, BeeConst.YES);
  }

  public static boolean toBoolean(int x) {
    return x == BeeConst.INT_TRUE;
  }

  public static boolean isBoolean(String s) {
    if (s == null)
      return false;
    else
      return same(s, BeeConst.STRING_TRUE) || same(s, BeeConst.YES)
          || same(s, BeeConst.STRING_FALSE) || same(s, BeeConst.NO);
  }

  public static boolean isBoolean(int x) {
    return x == BeeConst.INT_TRUE || x == BeeConst.INT_FALSE;
  }

  public static String toSeconds(long millis) {
    return Long.toString(millis / 1000) + BeeConst.STRING_POINT
        + toLeadingZeroes((int) (millis % 1000), 3);
  }

  public static boolean isDigit(CharSequence s) {
    if (s == null)
      return false;

    int len = s.length();
    if (len < 1)
      return false;

    boolean ok = true;
    char c;

    for (int i = 0; i < len; i++) {
      c = s.charAt(i);
      if (c < BeeConst.CHAR_ZERO || c > BeeConst.CHAR_NINE) {
        ok = false;
        break;
      }
    }

    return ok;
  }
}
