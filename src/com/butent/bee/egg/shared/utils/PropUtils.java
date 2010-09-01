package com.butent.bee.egg.shared.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;

public abstract class PropUtils {
  public static List<StringProp> EMPTY_STRING_PROP_LIST = new ArrayList<StringProp>();
  public static List<SubProp> EMPTY_PROP_SUB_LIST = new ArrayList<SubProp>();

  public static <T> boolean addBee(Collection<BeeProp<T>> lst, String nm, T v) {
    if (validName(nm) && validValue(v)) {
      lst.add(new BeeProp<T>(nm, v));
      return true;
    } else
      return false;
  }

  public static <T> int addHive(Collection<BeeProp<T>> lst, Object... x) {
    int c = x.length;
    int r = 0;

    if (c < 2)
      return r;

    for (int i = 0; i < c - 1; i += 2)
      if (x[i] instanceof String)
        if (addBee(lst, (String) x[i], (T) x[i + 1]))
          r++;

    return r;
  }

  public static int addString(Collection<StringProp> lst, Object... x) {
    int c = x.length;
    Assert.parameterCount(c + 1, 3);
    int r = 0;

    if (c < 2)
      return r;

    for (int i = 0; i < c - 1; i += 2)
      if (x[i] instanceof String && validName((String) x[i])
          && validValue(x[i + 1])) {
        lst.add(new StringProp((String) x[i], transformValue(x[i + 1])));
        r++;
      }

    return r;
  }

  public static boolean addSub(Collection<SubProp> lst, String nm, Object sub,
      Object v) {
    if (validName(nm) && validValue(v)) {
      lst.add(new SubProp(nm, transformSub(sub), transformValue(v)));
      return true;
    } else
      return false;
  }

  public static boolean addSub(Collection<SubProp> lst, String nm, Object v) {
    return addSub(lst, nm, null, v);
  }

  public static int addPropSub(Collection<SubProp> lst, boolean subMd,
      Object... x) {
    int c = x.length;
    int r = 0;
    boolean ok;

    int step = subMd ? 3 : 2;
    if (c < step)
      return r;

    for (int i = 0; i <= c - step; i += step) {
      if (!(x[i] instanceof String))
        continue;

      if (subMd)
        ok = addSub(lst, (String) x[i], x[i + 1], x[i + 2]);
      else
        ok = addSub(lst, (String) x[i], null, x[i + 1]);

      if (ok)
        r++;
    }

    return r;
  }

  public static void appendSub(Collection<SubProp> dst, Collection<SubProp> src) {
    if (dst != null && src != null && !src.isEmpty())
      dst.addAll(src);
  }

  public static void appendString(Collection<SubProp> dst, String root,
      Collection<StringProp> src) {
    Assert.notNull(dst);
    Assert.notEmpty(root);

    if (src != null && !src.isEmpty()) {
      StringProp el;

      for (Iterator<StringProp> it = src.iterator(); it.hasNext();) {
        el = it.next();
        addSub(dst, root, el.getName(), el.getValue());
      }
    }
  }

  public static void appendStringProp(Collection<StringProp> dst, String root,
      Collection<StringProp> src) {
    Assert.notNull(dst);
    if (src != null && !src.isEmpty()) {
      if (BeeUtils.isEmpty(root))
        dst.addAll(src);
      else {
        StringProp el;

        for (Iterator<StringProp> it = src.iterator(); it.hasNext();) {
          el = it.next();
          addString(dst, BeeUtils.concat(1, root, el.getName()), el.getValue());
        }
      }
    }
  }

  public static int addSplit(Collection<SubProp> lst, String nm, Object sub,
      String v, String sep) {
    int r = 0;

    if (lst == null || !validName(nm) || BeeUtils.isEmpty(v))
      return r;

    String z;
    if (sep != null && sep.length() > 0)
      z = sep;
    else
      z = BeeConst.STRING_COMMA;

    if (v.contains(z)) {
      String[] arr = v.split(z);
      if (arr.length > 1 && addSub(lst, nm, sub, BeeUtils.bracket(arr.length)))
        r++;

      for (int i = 0; i < arr.length; i++)
        if (addSub(lst, nm, sub, arr[i]))
          r++;
    } else if (addSub(lst, nm, sub, v))
      r++;

    return r;
  }

  public static int addRoot(Collection<SubProp> lst, String root, Object... x) {
    Assert.notNull(lst);
    Assert.notEmpty(root);

    int c = x.length;
    Assert.isTrue(c >= 2);

    int r = 0;

    for (int i = 0; i < c - 1; i += 2)
      if (addSub(lst, root, x[i], x[i + 1]))
        r++;

    return r;
  }

  public static List<StringProp> createStringProp(Object... obj) {
    List<StringProp> lst = new ArrayList<StringProp>();

    if (obj.length > 0)
      addString(lst, obj);

    return lst;
  }

  private static boolean validName(String nm) {
    return (nm != null && !nm.isEmpty());
  }

  private static boolean validValue(Object v) {
    if (v == null)
      return false;
    else if (v instanceof String)
      return !((String) v).isEmpty();
    else
      return true;
  }

  private static String transformSub(Object s) {
    return transformValue(s);
  }

  private static String transformValue(Object v) {
    if (v == null)
      return BeeConst.STRING_EMPTY;
    else if (v instanceof String)
      return transformString((String) v);
    else
      return BeeUtils.transform(v);
  }

  private static String transformString(String v) {
    if (v.isEmpty()) {
      return v;
    } else if (v.trim().isEmpty()) {
      if (v.length() <= 10) {
        return "[hex] " + BeeUtils.toHex(v.toCharArray());
      } else {
        return "[whitespace] " + v.length();
      }
    } else {
      return v;
    }
  }

}
