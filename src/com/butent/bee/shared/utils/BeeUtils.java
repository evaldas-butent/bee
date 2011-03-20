package com.butent.bee.shared.utils;

import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;
import com.butent.bee.shared.HasLength;
import com.butent.bee.shared.Transformable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeeUtils {
  private static int nameCounter = 0;

  public static String addName(String nm, Object v) {
    if (isEmpty(v, EnumSet.of(BeeType.NUMBER, BeeType.BOOLEAN))) {
      return BeeConst.STRING_EMPTY;
    } else if (isEmpty(nm)) {
      return transform(v);
    } else {
      return nm.trim() + BeeConst.DEFAULT_VALUE_SEPARATOR + transform(v);
    }
  }

  public static boolean allEmpty(Object... obj) {
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

  public static boolean allNotEmpty(Object... obj) {
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

  public static boolean betweenExclusive(int x, int min, int max) {
    return x >= min && x < max;
  }

  public static boolean betweenInclusive(int x, int min, int max) {
    return x >= min && x <= max;
  }

  public static String bracket(Object x) {
    String s = transform(x);

    if (s.isEmpty()) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeConst.STRING_LEFT_BRACKET + s + BeeConst.STRING_RIGHT_BRACKET;
    }
  }

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

  public static int compare(double x1, double x2) {
    if (x1 == x2 || ((Double) x1).equals(x2)) {
      return BeeConst.COMPARE_EQUAL;
    } else if (x1 < x2) {
      return BeeConst.COMPARE_LESS;
    } else {
      return BeeConst.COMPARE_MORE;
    }
  }

  public static int compare(int x1, int x2) {
    if (x1 == x2) {
      return BeeConst.COMPARE_EQUAL;
    } else if (x1 < x2) {
      return BeeConst.COMPARE_LESS;
    } else {
      return BeeConst.COMPARE_MORE;
    }
  }

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

  public static int compareNormalized(String s1, String s2) {
    return compare(normalize(s1), normalize(s2));
  }

  public static String concat(Object... x) {
    int c = x.length;
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
        s.append(transform(x[i], sep));
      }
    }

    return s.toString();
  }

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
  
  public static boolean context(CharSequence ctxt, CharSequence src) {
    if (ctxt == null || src == null || ctxt.length() == 0 || src.length() == 0) {
      return false;
    } else {
      return src.toString().toLowerCase().contains(ctxt.toString().toLowerCase());
    }
  }

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

  public static String createUniqueName() {
    return createUniqueName(null);
  }

  public static String createUniqueName(String pfx) {
    nameCounter++;

    if (pfx == null) {
      return transform(nameCounter);
    } else {
      return pfx.trim() + nameCounter;
    }
  }

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

  public static String elapsedSeconds(long start) {
    return bracket(toSeconds(System.currentTimeMillis() - start));
  }

  public static boolean equals(Object x, Object y) {
    if (x == null) {
      return y == null;
    } else {
      return x.equals(y);
    }
  }

  public static boolean equalsTrim(String s1, String s2) {
    if (s1 == null) {
      return isEmpty(s2);
    } else if (s2 == null) {
      return isEmpty(s1);
    } else {
      return s1.trim().equals(s2.trim());
    }
  }

  public static int exp10(int z) {
    Assert.nonNegative(z);
    Double x = Math.pow(10, z);
    Assert.isTrue(x < Integer.MAX_VALUE);
    return x.intValue();
  }

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

  public static int fitStart(int start, int len, int end) {
    if (start + len <= end) {
      return start;
    } else {
      return end - len;
    }
  }

  public static int fitStart(int start, int len, int end, int min) {
    return max(fitStart(start, len, end), min);
  }

  public static char[] fromHex(String s) {
    if (!isHexString(s)) {
      return null;
    }

    if (s.length() <= 4) {
      return new char[]{(char) Integer.parseInt(s, 16)};
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

  public static String ifString(Object x, String def) {
    if (x instanceof String && !isEmpty(x)) {
      return (String) x;
    } else {
      return def;
    }
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

  public static String increment(Object obj) {
    return increment(transform(obj));
  }

  public static String increment(String s) {
    return Integer.toString(toInt(s) + 1);
  }

  public static <T extends Comparable<T>> boolean inList(T x, T... lst) {
    boolean ok = false;

    for (int i = 0; i < lst.length; i++) {
      if (x.compareTo(lst[i]) == 0) {
        ok = true;
        break;
      }
    }

    return ok;
  }

  public static boolean inListIgnoreCase(String x, String... lst) {
    Assert.notEmpty(x);
    boolean ok = false;

    for (int i = 0; i < lst.length; i++) {
      if (x.equalsIgnoreCase(lst[i])) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  public static boolean inListSame(String x, String... lst) {
    Assert.notEmpty(x);
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

  public static String insert(String src, int pos, char c) {
    Assert.notNull(src);
    Assert.nonNegative(pos);
    Assert.isTrue(pos <= src.length());

    return new StringBuilder(src).insert(pos, c).toString();
  }

  public static String insert(String src, int pos, CharSequence cs) {
    Assert.notNull(src);
    Assert.nonNegative(pos);
    Assert.isTrue(pos <= src.length());
    Assert.hasLength(cs);

    return new StringBuilder(src).insert(pos, cs).toString();
  }

  public static boolean instanceOfFloatingPoint(Object x) {
    if (x == null) {
      return false;
    } else {
      return (x instanceof Float || x instanceof Double);
    }
  }

  public static boolean instanceOfIntegerType(Object x) {
    if (x == null) {
      return false;
    } else {
      return (x instanceof Byte || x instanceof Short || x instanceof Integer
          || x instanceof Long || x instanceof BigInteger || x instanceof BigDecimal);
    }
  }

  public static boolean instanceOfStringType(Object x) {
    if (x == null) {
      return false;
    } else {
      return (x instanceof String || x instanceof StringBuilder || x instanceof StringBuffer);
    }
  }

  public static boolean isBoolean(int x) {
    return x == BeeConst.INT_TRUE || x == BeeConst.INT_FALSE;
  }

  public static boolean isBoolean(String s) {
    if (s == null) {
      return false;
    } else {
      return same(s, BeeConst.STRING_TRUE) || same(s, BeeConst.YES)
          || same(s, BeeConst.STRING_FALSE) || same(s, BeeConst.NO);
    }
  }

  public static boolean isDigit(char c) {
    return c >= BeeConst.CHAR_ZERO && c <= BeeConst.CHAR_NINE;
  }

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

  public static boolean isDouble(double x) {
    return !Double.isNaN(x) && !Double.isInfinite(x);
  }

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
    } else {
      ok = false;
    }
    return ok;
  }

  public static boolean isEmpty(Object x, Set<BeeType> orType) {
    if (filterType(x, orType)) {
      return false;
    } else {
      return isEmpty(x);
    }
  }

  public static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F');
  }

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

  public static boolean isIdentifier(CharSequence name) {
    if (isEmpty(name)) {
      return false;
    }
    if (Character.isDigit(name.charAt(0))) {
      return false;
    }

    boolean ok = true;
    char c;
    for (int i = 0; i < name.length(); i++) {
      c = name.charAt(i);
      if (c != BeeConst.CHAR_UNDER && !Character.isLetterOrDigit(c)) {
        ok = false;
        break;
      }
    }
    return ok;
  }

  public static boolean isIndex(Object obj, int idx) {
    if (obj == null || idx < 0) {
      return false;
    } else {
      int n = length(obj);
      return (n > 0 && idx < n);
    }
  }

  public static boolean isInt(double x) {
    return isDouble(x) && x > Integer.MIN_VALUE && x < Integer.MAX_VALUE;
  }

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

  public static boolean isLong(double x) {
    return isDouble(x) && x > Long.MIN_VALUE && x < Long.MAX_VALUE;
  }

  public static boolean isOrdinal(Class<?> clazz, int idx) {
    if (clazz == null || !clazz.isEnum() || idx < 0) {
      return false;
    }
    return idx < ArrayUtils.length(clazz.getEnumConstants());
  }

  public static boolean isPositive(Object x) {
    if (x instanceof Integer) {
      return (Integer) x > 0;
    } else if (x instanceof Number) {
      return Double.compare(((Number) x).doubleValue(), Double.valueOf(BeeConst.DOUBLE_ZERO)) > 0;
    } else {
      return false;
    }
  }

  public static <T extends Comparable<T>> boolean isPositive(T x) {
    if (x instanceof Number) {
      T z = zero(x);
      return x.compareTo(z) > 0;
    } else {
      return false;
    }
  }

  public static boolean isPrefix(CharSequence src, char pfx) {
    if (src == null || src.length() <= 0) {
      return false;
    }
    return src.charAt(0) == pfx;
  }

  public static boolean isPrefixOrSuffix(CharSequence src, char ch) {
    return (isPrefix(src, ch) || isSuffix(src, ch)) && !containsOnly(src, ch);
  }
  
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
  
  public static boolean isTrue(Object obj) {
    if (obj == null) {
      return false;
    } else if (obj instanceof Boolean) {
      return (Boolean) obj;
    } else {
      return false;
    }
  }

  public static boolean isZero(BigDecimal x) {
    return x == BigDecimal.ZERO;
  }

  public static boolean isZero(BigInteger x) {
    return x == BigInteger.ZERO;
  }

  public static boolean isZero(byte x) {
    return x == 0;
  }

  public static boolean isZero(double x) {
    return x == Double.valueOf(BeeConst.DOUBLE_ZERO);
  }

  public static boolean isZero(float x) {
    return x == Float.valueOf(BeeConst.FLOAT_ZERO);
  }

  public static boolean isZero(int x) {
    return x == 0;
  }

  public static boolean isZero(long x) {
    return x == 0L;
  }

  public static boolean isZero(Object x) {
    if (x instanceof Number) {
      return ((Number) x).doubleValue() == Double.valueOf(BeeConst.DOUBLE_ZERO);
    } else {
      return false;
    }
  }

  public static boolean isZero(short x) {
    return x == 0;
  }

  public static <T> List<T> join(List<? extends T>... src) {
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
      len = ((HasLength) x).length();
    } else if (ArrayUtils.isArray(x)) {
      len = ArrayUtils.length(x);
    } else {
      len = 0;
    }
    return len;
  }

  public static int limit(int x, int min, int max) {
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

  public static double limit(double x, double min, double max) {
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

  public static <T> T listGetQuietly(List<? extends T> lst, int idx) {
    if (isIndex(lst, idx)) {
      return lst.get(idx);
    } else {
      return null;
    }
  }

  public static <T extends Comparable<T>> T max(T... x) {
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

  public static <T extends Comparable<T>> T min(T... x) {
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

  public static String normalize(String s) {
    if (s == null) {
      return BeeConst.STRING_EMPTY;
    }
    return s.trim().toLowerCase();
  }

  public static String normSep(Object x) {
    String sep;

    if (x instanceof String && length(x) > 0) {
      sep = (String) x;
    } else if (x instanceof Number) {
      sep = space(((Number) x).intValue());
    } else if (x instanceof Character) {
      sep = new String(new char[]{(Character) x});
    } else if (x instanceof CharSequence && length(x) > 0) {
      sep = ((CharSequence) x).toString();
    } else {
      sep = BeeConst.DEFAULT_LIST_SEPARATOR;
    }
    return sep;
  }

  public static String normSep(Object x, Object def) {
    String sep;

    if (x instanceof CharSequence && length(x) > 0 || isPositive(x) || x instanceof Character) {
      sep = normSep(x);
    } else {
      sep = normSep(def);
    }

    return sep;
  }

  public static <T> T nvl(T... obj) {
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

  public static String parenthesize(Object x) {
    String s = transform(x);

    if (s.isEmpty()) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeConst.STRING_LEFT_PARENTHESIS + s + BeeConst.STRING_RIGHT_PARENTHESIS;
    }
  }

  public static String progress(int pos, int tot) {
    return Integer.toString(pos) + BeeConst.DEFAULT_PROGRESS_SEPARATOR + Integer.toString(tot);
  }

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

  public static int randomInt(int min, int max) {
    Assert.isTrue(max > min + 1);

    Double z = Math.floor(Math.random() * (max - min));
    return min + z.intValue();
  }

  public static long randomLong(long min, long max) {
    Assert.isTrue(max > min + 1);

    Double z = Math.floor(Math.random() * (max - min));
    return min + z.longValue();
  }

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
  
  public static String removePrefix(String str, char pfx) {
    if (isPrefix(str, pfx)) {
      return removePrefix(str.substring(1), pfx);
    }
    return str;
  }

  public static String removePrefixAndSuffix(String str, char ch) {
    return removeSuffix(removePrefix(str, ch), ch);
  }
  
  public static String removeSuffix(String str, char sfx) {
    if (isSuffix(str, sfx)) {
      return removeSuffix(str.substring(0, str.length() - 1), sfx);
    }
    return str;
  }
  
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
    if (!isDigit(str.substring(0, p))) {
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

  public static String replace(String src, int start, int end, char c) {
    return replace(src, start, end, String.valueOf(c));
  }

  public static String replace(String src, int start, int end, String str) {
    Assert.isIndex(src, start);
    Assert.isIndex(src, end - 1);
    Assert.notNull(str);

    return new StringBuilder(src).replace(start, end, str).toString();
  }

  public static String replace(String text, String search, String replacement) {
    return replace(text, search, replacement, -1);
  }

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

    while (end >= 0) {
      sb.append(text.substring(start, end)).append(replacement);
      start = end + len;
      if (--max == 0) {
        break;
      }
      end = text.indexOf(search, start);
    }
    sb.append(text.substring(start));

    return sb.toString();
  }

  public static String replicate(char z, int n) {
    Assert.isPositive(n);

    char[] arr = new char[n];
    Arrays.fill(arr, z);
    return new String(arr);
  }

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

  public static boolean same(String s1, String s2) {
    if (isEmpty(s1)) {
      return isEmpty(s2);
    }
    if (isEmpty(s2)) {
      return isEmpty(s1);
    }
    return s1.trim().equalsIgnoreCase(s2.trim());
  }

  public static String space(int l) {
    if (l == 1) {
      return BeeConst.STRING_SPACE;
    } else if (l < 1) {
      return BeeConst.STRING_EMPTY;
    } else {
      return replicate(BeeConst.CHAR_SPACE, l);
    }
  }

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
      return new String[]{str.trim()};
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

  public static boolean toBoolean(int x) {
    return x == BeeConst.INT_TRUE;
  }

  public static boolean toBoolean(String s) {
    if (isEmpty(s)) {
      return false;
    }
    return BeeConst.isTrue(s.trim().charAt(0));
  }

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

  public static int toInt(boolean b) {
    return b ? BeeConst.INT_TRUE : BeeConst.INT_FALSE;
  }

  public static int toInt(String s) {
    if (isEmpty(s)) {
      return 0;
    }
    int i;

    try {
      i = Integer.parseInt(s.trim());
    } catch (NumberFormatException ex) {
      i = 0;
    }
    return i;
  }

  public static String toLeadingZeroes(int x, int n) {
    if (x >= 0 && n > 0) {
      return padLeft(((Integer) x).toString(), n, BeeConst.CHAR_ZERO);
    } else {
      return ((Integer) x).toString();
    }
  }

  public static long toLong(String s) {
    if (isEmpty(s)) {
      return 0L;
    }
    long x;

    try {
      x = Long.parseLong(s.trim());
    } catch (NumberFormatException ex) {
      x = 0L;
    }
    return x;
  }

  public static String toSeconds(long millis) {
    return Long.toString(millis / 1000) + BeeConst.STRING_POINT
        + toLeadingZeroes((int) (millis % 1000), 3);
  }

  public static String toString(boolean b) {
    return b ? BeeConst.STRING_TRUE : BeeConst.STRING_FALSE;
  }

  public static String toString(double x) {
    return Double.toString(x);
  }

  public static String toString(int x) {
    return Integer.toString(x);
  }

  public static String toString(long x) {
    return Long.toString(x);
  }

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

  public static String transform(Object x, Object... sep) {
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

  public static String transformClass(Object obj) {
    if (obj == null) {
      return BeeConst.NULL;
    } else {
      return obj.getClass().getName();
    }
  }

  public static String transformCollection(Collection<?> col, Object... sep) {
    if (isEmpty(col)) {
      return BeeConst.STRING_EMPTY;
    }

    int cSep = sep.length;
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
      nextSep = new String[]{z};
    }

    for (Iterator<?> it = col.iterator(); it.hasNext();) {
      el = it.next();
      if (sb.length() > 0) {
        sb.append(z);
      }
      sb.append(transform(el, nextSep));
    }
    return sb.toString();
  }

  public static String transformEnumeration(Enumeration<?> src, Object... sep) {
    if (isEmpty(src)) {
      return BeeConst.STRING_EMPTY;
    }
    int cSep = sep.length;
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
      nextSep = new String[]{z};
    }

    while (src.hasMoreElements()) {
      el = src.nextElement();
      if (sb.length() > 0) {
        sb.append(z);
      }
      sb.append(transform(el, nextSep));
    }
    return sb.toString();
  }

  public static String transformMap(Map<?, ?> map, Object... sep) {
    if (isEmpty(map)) {
      return BeeConst.STRING_EMPTY;
    }
    int cSep = sep.length;
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
      nextSep = new String[]{z};
    }

    for (Map.Entry<?, ?> el : map.entrySet()) {
      key = el.getKey();
      value = el.getValue();

      if (sb.length() > 0) {
        sb.append(z);
      }
      sb.append(addName(transform(key), transform(value, nextSep)));
    }
    return sb.toString();
  }

  public static String transformNoTrim(Object x) {
    if (x instanceof String) {
      return (String) x;
    } else {
      return transform(x);
    }
  }

  public static String transformOptions(Object... opt) {
    int c = opt.length;
    if (c < 2) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    String el;

    for (int i = 0; i < c - 1; i += 2) {
      if (opt[i] instanceof String) {
        el = addName((String) opt[i], opt[i + 1]);
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
