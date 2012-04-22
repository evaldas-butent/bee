package com.butent.bee.shared.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class NameUtils {

  public static final char QUALIFIED_NAME_SEPARATOR = ':';

  public static final Splitter NAME_SPLITTER = 
      Splitter.on(CharMatcher.anyOf(" ,;")).trimResults().omitEmptyStrings();
  
  private static int nameCounter = 0;

  /**
   * Adds a value to a name. Object {@code v} must be either numeric or boolean. Name and value are
   * separated by a default separator.
   * 
   * @param nm name
   * @param v a value
   * @return a formed String
   */
  public static String addName(String nm, Object v) {
    if (BeeUtils.isEmpty(v, EnumSet.of(BeeType.NUMBER, BeeType.BOOLEAN))) {
      return BeeConst.STRING_EMPTY;
    } else if (BeeUtils.isEmpty(nm)) {
      return BeeUtils.transform(v);
    } else {
      return nm.trim() + BeeConst.DEFAULT_VALUE_SEPARATOR + BeeUtils.transform(v);
    }
  }

  public static String camelize(String str, char sep) {
    if (str == null || str.indexOf(sep) < 0) {
      return str;
    }

    char[] arr = str.toCharArray();
    int cnt = 0;
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] != sep) {
        if (cnt < i) {
          arr[cnt] = arr[i];
        }
        cnt++;
      } else if (i < arr.length - 1) {
        arr[i + 1] = Character.toUpperCase(arr[i + 1]);
      }
    }
    if (cnt > 0) {
      return new String(arr, 0, cnt);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }
  
  /**
   * Creates a unique name.
   * 
   * @return a unique name.
   */
  public static String createUniqueName() {
    return createUniqueName(null);
  }

  /**
   * Created a unique name with a specified prefix.
   * 
   * @param pfx prefix used for generating a unique name
   * @return String which contains unique value with a specified prefix.
   */
  public static String createUniqueName(String pfx) {
    nameCounter++;

    if (pfx == null) {
      return BeeUtils.toString(nameCounter);
    } else {
      return pfx.trim() + nameCounter;
    }
  }
  
  public static String decamelize(String str, char sep) {
    if (BeeUtils.isEmpty(str)) {
      return str;
    }
    if (str.equals(str.toLowerCase())) {
      return str;
    }

    StringBuilder sb = new StringBuilder();
    char ch;

    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if (Character.isUpperCase(ch)) {
        sb.append(sep).append(Character.toLowerCase(ch));
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }
  
  /**
   * @param cls the class to get a name from
   * @return only the String class name with packages excluded.
   */
  public static String getClassName(Class<?> cls) {
    String name = cls.getName();
    int p = Math.max(name.lastIndexOf('.'), name.lastIndexOf('$'));
    return name.substring(p + 1);
  }

  public static <E extends Enum<?>> E getConstant(Class<E> clazz, String name) {
    Assert.notNull(clazz);
    if (BeeUtils.isEmpty(name)) {
      return null;
    }

    E result = null;

    for (int i = 0; i < 3; i++) {
      for (E constant : clazz.getEnumConstants()) {
        if (i == 0) {
          if (BeeUtils.same(constant.name(), name)) {
            result = constant;
            break;
          }

        } else if (i == 1) {
          if (BeeUtils.startsSame(constant.name(), name)) {
            if (result == null) {
              result = constant;
            } else {
              result = null;
              break;
            }
          }

        } else {
          if (BeeUtils.context(name, constant.name())) {
            if (result == null) {
              result = constant;
            } else {
              result = null;
              break;
            }
          }
        }
      }
      if (result != null) {
        break;
      }
    }
    return result;
  }
  
  public static String getLocalPart(String qName) {
    if (BeeUtils.contains(qName, QUALIFIED_NAME_SEPARATOR)) {
      return BeeUtils.getSuffix(qName, QUALIFIED_NAME_SEPARATOR);
    } else {
      return qName;
    }
  }

  public static String getName(Object obj) {
    return getClassName(Assert.notNull(obj).getClass());
  }
  
  public static <E extends Enum<?>> String getName(Class<E> clazz, Integer idx) {
    if (clazz == null || idx == null || idx < 0) {
      return null;
    }

    E[] constants = clazz.getEnumConstants();
    if (constants != null && idx < constants.length) {
      return constants[idx].name();
    } else {
      return null;
    }
  }

  public static String getNamespacePrefix(String qName) {
    return BeeUtils.getPrefix(qName, QUALIFIED_NAME_SEPARATOR);
  }
  
  public static String getWord(String s, int idx) {
    if (BeeUtils.isEmpty(s) || idx < 0) {
      return null;
    }
    int i = 0;
    for (String w : NAME_SPLITTER.split(s)) {
      if (idx == i++) {
        return w;
      }
    }
    return null;
  }
  
  /**
   * Checks if a string is a correct identifier. Identifier cannot start with a number, and can only
   * contain "_", digits and letters.
   * 
   * @param name the name to check
   * @return true if the String is a correct identifier, false otherwise.
   */
  public static boolean isIdentifier(CharSequence name) {
    if (BeeUtils.isEmpty(name)) {
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
  
  public static List<String> toList(String s) {
    if (s == null) {
      return null;
    }
    return Lists.newArrayList(NAME_SPLITTER.split(s));
  }
  
  public static Set<String> toSet(String s) {
    if (s == null) {
      return null;
    }
    return Sets.newHashSet(NAME_SPLITTER.split(s));
  }
  
  /**
   * Transforms an Object {@code obj} to a String representation. In general, this method returns a
   * string that "textually represents" a class(class name).
   * 
   * @param obj value to transform
   * @return Object's class name
   */
  public static String transformClass(Object obj) {
    if (obj == null) {
      return BeeConst.NULL;
    } else {
      return obj.getClass().getName();
    }
  }
  
  private NameUtils() {
  }
}
